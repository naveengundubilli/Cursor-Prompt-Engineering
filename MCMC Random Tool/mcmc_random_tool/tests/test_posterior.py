"""Tests for posterior module."""

import numpy as np
import pytest

from mcmc_random_tool.posterior import (
    dirichlet_sample_row,
    posterior_predictive_sequences,
    sample_transition_matrix_posterior,
)


def test_dirichlet_sample_row():
    """Test sampling from Dirichlet distribution."""
    alpha_vec = np.array([1.0, 2.0, 3.0])
    rng = np.random.default_rng(42)

    result = dirichlet_sample_row(alpha_vec, rng)

    # Check shape
    assert result.shape == alpha_vec.shape

    # Check probabilities sum to 1
    assert np.isclose(result.sum(), 1.0)

    # Check all probabilities are non-negative
    assert np.all(result >= 0)


def test_dirichlet_sample_row_negative_alpha():
    """Test Dirichlet sampling with negative alpha."""
    alpha_vec = np.array([1.0, -1.0, 3.0])
    rng = np.random.default_rng(42)

    with pytest.raises(ValueError, match="Alpha parameters must be non-negative"):
        dirichlet_sample_row(alpha_vec, rng)


def test_dirichlet_sample_row_zero_sum():
    """Test Dirichlet sampling when sum is zero."""
    alpha_vec = np.array([0.0, 0.0, 0.0])
    rng = np.random.default_rng(42)

    result = dirichlet_sample_row(alpha_vec, rng)

    # Should return uniform distribution
    expected = np.array([1/3, 1/3, 1/3])
    assert np.allclose(result, expected)


def test_sample_transition_matrix_posterior():
    """Test sampling transition matrix from posterior."""
    sequences = [[1, 2, 3], [2, 3, 1], [1, 2, 1]]
    states = [1, 2, 3]
    rng = np.random.default_rng(42)

    matrix = sample_transition_matrix_posterior(sequences, states, alpha=1.0, rng=rng)

    # Check shape
    assert matrix.shape == (3, 3)

    # Check rows sum to 1
    assert np.allclose(matrix.sum(axis=1), 1.0)

    # Check all probabilities are non-negative
    assert np.all(matrix >= 0)


def test_sample_transition_matrix_posterior_invalid_alpha():
    """Test posterior sampling with invalid alpha."""
    sequences = [[1, 2, 3]]
    states = [1, 2, 3]
    rng = np.random.default_rng(42)

    with pytest.raises(ValueError, match="Alpha must be positive"):
        sample_transition_matrix_posterior(sequences, states, alpha=0.0, rng=rng)


def test_sample_transition_matrix_posterior_empty_sequences():
    """Test posterior sampling with empty sequences."""
    sequences = []
    states = [1, 2, 3]
    rng = np.random.default_rng(42)

    with pytest.raises(ValueError, match="No sequences provided"):
        sample_transition_matrix_posterior(sequences, states, alpha=1.0, rng=rng)


def test_sample_transition_matrix_posterior_reproducibility():
    """Test that posterior sampling is reproducible with same seed."""
    sequences = [[1, 2, 3], [2, 3, 1]]
    states = [1, 2, 3]

    rng1 = np.random.default_rng(42)
    matrix1 = sample_transition_matrix_posterior(sequences, states, alpha=1.0, rng=rng1)

    rng2 = np.random.default_rng(42)
    matrix2 = sample_transition_matrix_posterior(sequences, states, alpha=1.0, rng=rng2)

    # Should be identical with same seed
    assert np.allclose(matrix1, matrix2)


def test_posterior_predictive_sequences():
    """Test generating posterior predictive sequences."""
    sequences = [[1, 2, 3], [2, 3, 1]]
    states = [1, 2, 3]
    start_probs = np.array([0.5, 0.3, 0.2])
    rng = np.random.default_rng(42)

    sims = posterior_predictive_sequences(
        sequences, states, start_probs,
        alpha=1.0, length=3, nsamples=10, rng=rng
    )

    # Check number of samples
    assert len(sims) == 10

    # Check each sample has correct structure
    for seq, lp in sims:
        assert len(seq) == 3
        assert all(state in states for state in seq)
        assert np.isfinite(lp) or lp == -np.inf


def test_posterior_predictive_sequences_invalid_alpha():
    """Test posterior predictive with invalid alpha."""
    sequences = [[1, 2, 3]]
    states = [1, 2, 3]
    start_probs = np.array([0.5, 0.3, 0.2])
    rng = np.random.default_rng(42)

    with pytest.raises(ValueError, match="Alpha must be positive"):
        posterior_predictive_sequences(
            sequences, states, start_probs,
            alpha=0.0, length=3, nsamples=10, rng=rng
        )


def test_posterior_predictive_sequences_invalid_length():
    """Test posterior predictive with invalid length."""
    sequences = [[1, 2, 3]]
    states = [1, 2, 3]
    start_probs = np.array([0.5, 0.3, 0.2])
    rng = np.random.default_rng(42)

    with pytest.raises(ValueError, match="Length must be positive"):
        posterior_predictive_sequences(
            sequences, states, start_probs,
            alpha=1.0, length=0, nsamples=10, rng=rng
        )


def test_posterior_predictive_sequences_invalid_nsamples():
    """Test posterior predictive with invalid nsamples."""
    sequences = [[1, 2, 3]]
    states = [1, 2, 3]
    start_probs = np.array([0.5, 0.3, 0.2])
    rng = np.random.default_rng(42)

    with pytest.raises(ValueError, match="Number of samples must be positive"):
        posterior_predictive_sequences(
            sequences, states, start_probs,
            alpha=1.0, length=3, nsamples=0, rng=rng
        )


def test_posterior_predictive_sequences_reproducibility():
    """Test that posterior predictive is reproducible with same seed."""
    sequences = [[1, 2, 3]]
    states = [1, 2, 3]
    start_probs = np.array([0.5, 0.3, 0.2])

    rng1 = np.random.default_rng(42)
    sims1 = posterior_predictive_sequences(
        sequences, states, start_probs,
        alpha=1.0, length=3, nsamples=5, rng=rng1
    )

    rng2 = np.random.default_rng(42)
    sims2 = posterior_predictive_sequences(
        sequences, states, start_probs,
        alpha=1.0, length=3, nsamples=5, rng=rng2
    )

    # Should be identical with same seed
    assert len(sims1) == len(sims2)
    for (seq1, lp1), (seq2, lp2) in zip(sims1, sims2):
        assert seq1 == seq2
        assert np.isclose(lp1, lp2)
