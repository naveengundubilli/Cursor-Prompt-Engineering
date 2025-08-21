"""Markov chain model with Dirichlet smoothing."""

import logging
from collections import Counter, defaultdict
from typing import Optional

import numpy as np

logger = logging.getLogger(__name__)


def build_transition_counts(
    sequences: list[list[int]]
) -> tuple[dict[int, Counter], list[int]]:
    """
    Build transition count matrix from sequences.

    Args:
        sequences: List of sequences, where each sequence is a list of integers

    Returns:
        Tuple of (counts_dict, sorted_states) where counts_dict maps
        state -> Counter of next states, and sorted_states is sorted list of states

    Raises:
        ValueError: If no sequences provided
    """
    if not sequences:
        raise ValueError("No sequences provided")

    counts: dict[int, Counter] = defaultdict(Counter)
    states: set[int] = set()

    for seq in sequences:
        if len(seq) < 2:
            continue

        for i in range(len(seq) - 1):
            current_state = seq[i]
            next_state = seq[i + 1]
            counts[current_state][next_state] += 1
            states.add(current_state)
            states.add(next_state)

    if not states:
        raise ValueError("No valid states found in sequences")

    sorted_states = sorted(states)
    logger.info(f"Built transition counts for {len(sorted_states)} states from {len(sequences)} sequences")
    return counts, sorted_states


def transition_matrix_from_counts(
    counts: dict[int, Counter],
    states: list[int],
    alpha: float = 1.0
) -> np.ndarray:
    """
    Build smoothed transition matrix from counts.

    Args:
        counts: Transition counts dictionary
        states: List of state values
        alpha: Dirichlet smoothing parameter (must be > 0)

    Returns:
        Transition matrix with shape (n_states, n_states)

    Raises:
        ValueError: If alpha <= 0
    """
    if alpha <= 0:
        raise ValueError(f"Alpha must be positive, got {alpha}")

    n = len(states)
    state_to_idx = {state: i for i, state in enumerate(states)}
    matrix = np.zeros((n, n), dtype=np.float64)

    for i, state in enumerate(states):
        if state in counts:
            # Get counts for this state
            state_counts = counts[state]
            total = sum(state_counts.values()) + alpha * n

            for next_state, count in state_counts.items():
                if next_state in state_to_idx:
                    j = state_to_idx[next_state]
                    matrix[i, j] = (count + alpha) / total
                else:
                    logger.warning(f"Next state {next_state} not in states list")

            # Fill in missing transitions with alpha smoothing
            for j in range(n):
                if matrix[i, j] == 0:
                    matrix[i, j] = alpha / total
        else:
            # No transitions from this state, use uniform distribution
            matrix[i, :] = 1.0 / n

    # Ensure numeric stability
    assert np.all(matrix >= 0), "Negative probabilities found"
    assert np.allclose(matrix.sum(axis=1), 1.0), "Rows do not sum to 1"

    logger.info(f"Built transition matrix with alpha={alpha}")
    return matrix


def empirical_start_distribution(
    sequences: list[list[int]],
    states: list[int],
    alpha: float = 1.0
) -> np.ndarray:
    """
    Build smoothed start state distribution.

    Args:
        sequences: List of sequences
        states: List of state values
        alpha: Dirichlet smoothing parameter (must be > 0)

    Returns:
        Start state probabilities as numpy array

    Raises:
        ValueError: If alpha <= 0
    """
    if alpha <= 0:
        raise ValueError(f"Alpha must be positive, got {alpha}")

    if not sequences:
        raise ValueError("No sequences provided")

    n = len(states)
    state_to_idx = {state: i for i, state in enumerate(states)}
    start_counts = np.zeros(n, dtype=np.float64)

    for seq in sequences:
        if seq:
            first_state = seq[0]
            if first_state in state_to_idx:
                start_counts[state_to_idx[first_state]] += 1

    # Apply Dirichlet smoothing
    probs = (start_counts + alpha) / (start_counts.sum() + alpha * n)

    # Ensure numeric stability
    assert np.all(probs >= 0), "Negative start probabilities found"
    assert np.isclose(probs.sum(), 1.0), "Start probabilities do not sum to 1"

    logger.info(f"Built start distribution with alpha={alpha}")
    return probs


def argmax_sequence(
    start_state: int,
    states: list[int],
    trans_mat: np.ndarray,
    length: int = 7
) -> list[int]:
    """
    Generate most likely sequence using argmax transitions.

    Args:
        start_state: Starting state
        states: List of state values
        trans_mat: Transition matrix
        length: Sequence length

    Returns:
        Most likely sequence

    Raises:
        ValueError: If start_state not in states or length < 1
    """
    if length < 1:
        raise ValueError(f"Length must be positive, got {length}")

    state_to_idx = {state: i for i, state in enumerate(states)}
    if start_state not in state_to_idx:
        raise ValueError(f"Start state {start_state} not found in states")

    sequence = [start_state]
    current_state = start_state

    for _ in range(length - 1):
        current_idx = state_to_idx[current_state]
        next_idx = np.argmax(trans_mat[current_idx])
        next_state = states[next_idx]
        sequence.append(next_state)
        current_state = next_state

    return sequence


def sample_sequence(
    states: list[int],
    start_probs: np.ndarray,
    trans_mat: np.ndarray,
    length: int = 7,
    rng: Optional[np.random.Generator] = None
) -> list[int]:
    """
    Sample a sequence from the Markov chain.

    Args:
        states: List of state values
        start_probs: Start state probabilities
        trans_mat: Transition matrix
        length: Sequence length
        rng: Random number generator

    Returns:
        Sampled sequence

    Raises:
        ValueError: If length < 1 or invalid inputs
    """
    if length < 1:
        raise ValueError(f"Length must be positive, got {length}")

    if rng is None:
        rng = np.random.default_rng()

    n = len(states)
    if len(start_probs) != n or trans_mat.shape != (n, n):
        raise ValueError(f"Transition matrix shape {trans_mat.shape} != ({n}, {n})")

    idx_to_state = dict(enumerate(states))

    # Sample start state
    start_idx = rng.choice(n, p=start_probs)
    sequence = [idx_to_state[start_idx]]

    # Sample remaining states
    current_idx = start_idx
    for _ in range(length - 1):
        next_idx = rng.choice(n, p=trans_mat[current_idx])
        sequence.append(idx_to_state[next_idx])
        current_idx = next_idx

    return sequence


def logprob_of_sequence(
    seq: list[int],
    states: list[int],
    start_probs: np.ndarray,
    trans_mat: np.ndarray
) -> float:
    """
    Compute log probability of a sequence.

    Args:
        seq: Sequence to evaluate
        states: List of state values
        start_probs: Start state probabilities
        trans_mat: Transition matrix

    Returns:
        Log probability (may be -inf for impossible sequences)
    """
    if not seq:
        return 0.0

    state_to_idx = {state: i for i, state in enumerate(states)}

    # Check if all states in sequence are valid
    for state in seq:
        if state not in state_to_idx:
            return -np.inf

    # Start probability
    start_idx = state_to_idx[seq[0]]
    log_prob = np.log(start_probs[start_idx])

    # Transition probabilities
    for i in range(len(seq) - 1):
        current_idx = state_to_idx[seq[i]]
        next_idx = state_to_idx[seq[i + 1]]
        log_prob += np.log(trans_mat[current_idx, next_idx])

    return log_prob
