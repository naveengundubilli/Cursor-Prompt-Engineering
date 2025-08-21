"""Tests for model module."""

import numpy as np
import pytest

from mcmc_random_tool.model import (
    argmax_sequence,
    build_transition_counts,
    empirical_start_distribution,
    logprob_of_sequence,
    sample_sequence,
    transition_matrix_from_counts,
)


def test_build_transition_counts():
    """Test building transition counts from sequences."""
    sequences = [[1, 2, 3], [2, 3, 1], [1, 2, 1]]
    counts, states = build_transition_counts(sequences)

    assert states == [1, 2, 3]
    assert counts[1][2] == 2  # 1->2 appears twice (from [1,2,3] and [1,2,1])
    assert counts[2][3] == 2  # 2->3 appears twice (from [1,2,3] and [2,3,1])
    assert counts[2][1] == 1  # 2->1 appears once (from [1,2,1])
    assert counts[3][1] == 1  # 3->1 appears once (from [2,3,1])


def test_build_transition_counts_empty():
    """Test building counts with empty sequences."""
    with pytest.raises(ValueError, match="No sequences provided"):
        build_transition_counts([])


def test_build_transition_counts_single_state():
    """Test building counts with single state sequences."""
    sequences = [[1, 1, 1], [1, 1]]
    counts, states = build_transition_counts(sequences)

    assert states == [1]
    assert counts[1][1] == 3  # 1->1 appears 3 times


def test_transition_matrix_from_counts():
    """Test building transition matrix from counts."""
    sequences = [[1, 2, 3], [2, 3, 1]]
    counts, states = build_transition_counts(sequences)
    matrix = transition_matrix_from_counts(counts, states, alpha=1.0)

    # Check shape
    assert matrix.shape == (3, 3)

    # Check rows sum to 1
    assert np.allclose(matrix.sum(axis=1), 1.0)

    # Check all probabilities are non-negative
    assert np.all(matrix >= 0)


def test_transition_matrix_from_counts_invalid_alpha():
    """Test transition matrix with invalid alpha."""
    sequences = [[1, 2, 3]]
    counts, states = build_transition_counts(sequences)

    with pytest.raises(ValueError, match="Alpha must be positive"):
        transition_matrix_from_counts(counts, states, alpha=0.0)

    with pytest.raises(ValueError, match="Alpha must be positive"):
        transition_matrix_from_counts(counts, states, alpha=-1.0)


def test_empirical_start_distribution():
    """Test building start state distribution."""
    sequences = [[1, 2, 3], [2, 3, 1], [1, 2, 1]]
    states = [1, 2, 3]
    start_probs = empirical_start_distribution(sequences, states, alpha=1.0)

    # Check shape
    assert len(start_probs) == 3

    # Check probabilities sum to 1
    assert np.isclose(start_probs.sum(), 1.0)

    # Check all probabilities are non-negative
    assert np.all(start_probs >= 0)

    # State 1 appears twice as start, state 2 once
    # With alpha=1.0, should have higher probability for state 1
    assert start_probs[0] > start_probs[1]  # state 1 > state 2


def test_empirical_start_distribution_invalid_alpha():
    """Test start distribution with invalid alpha."""
    sequences = [[1, 2, 3]]
    states = [1, 2, 3]

    with pytest.raises(ValueError, match="Alpha must be positive"):
        empirical_start_distribution(sequences, states, alpha=0.0)


def test_argmax_sequence():
    """Test generating argmax sequence."""
    states = [1, 2, 3]
    # Create a deterministic transition matrix
    trans_mat = np.array([
        [0.1, 0.8, 0.1],  # From state 1, prefer state 2
        [0.1, 0.1, 0.8],  # From state 2, prefer state 3
        [0.8, 0.1, 0.1],  # From state 3, prefer state 1
    ])

    seq = argmax_sequence(1, states, trans_mat, length=4)
    assert seq == [1, 2, 3, 1]  # Should follow the highest probability path


def test_argmax_sequence_invalid_start():
    """Test argmax sequence with invalid start state."""
    states = [1, 2, 3]
    trans_mat = np.eye(3)

    with pytest.raises(ValueError, match="Start state.*not found"):
        argmax_sequence(5, states, trans_mat, length=3)


def test_argmax_sequence_invalid_length():
    """Test argmax sequence with invalid length."""
    states = [1, 2, 3]
    trans_mat = np.eye(3)

    with pytest.raises(ValueError, match="Length must be positive"):
        argmax_sequence(1, states, trans_mat, length=0)


def test_sample_sequence():
    """Test sampling sequence."""
    states = [1, 2, 3]
    start_probs = np.array([0.5, 0.3, 0.2])
    trans_mat = np.array([
        [0.1, 0.8, 0.1],
        [0.1, 0.1, 0.8],
        [0.8, 0.1, 0.1],
    ])

    rng = np.random.default_rng(42)  # Fixed seed for reproducibility
    seq = sample_sequence(states, start_probs, trans_mat, length=5, rng=rng)

    assert len(seq) == 5
    assert all(state in states for state in seq)


def test_sample_sequence_invalid_length():
    """Test sampling sequence with invalid length."""
    states = [1, 2, 3]
    start_probs = np.array([0.5, 0.3, 0.2])
    trans_mat = np.eye(3)

    with pytest.raises(ValueError, match="Length must be positive"):
        sample_sequence(states, start_probs, trans_mat, length=0)


def test_logprob_of_sequence():
    """Test computing log probability of sequence."""
    states = [1, 2, 3]
    start_probs = np.array([0.5, 0.3, 0.2])
    trans_mat = np.array([
        [0.1, 0.8, 0.1],
        [0.1, 0.1, 0.8],
        [0.8, 0.1, 0.1],
    ])

    # Test valid sequence
    seq = [1, 2, 3]
    lp = logprob_of_sequence(seq, states, start_probs, trans_mat)
    assert np.isfinite(lp)
    assert lp < 0  # Log probability should be negative

    # Test invalid sequence (state not in states)
    seq_invalid = [1, 2, 5]
    lp_invalid = logprob_of_sequence(seq_invalid, states, start_probs, trans_mat)
    assert lp_invalid == -np.inf

    # Test empty sequence
    lp_empty = logprob_of_sequence([], states, start_probs, trans_mat)
    assert lp_empty == 0.0
