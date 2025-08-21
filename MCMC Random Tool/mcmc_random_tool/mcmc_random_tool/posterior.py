"""Posterior sampling for Markov chain models."""

import logging
from typing import Optional

import numpy as np

from .model import build_transition_counts, logprob_of_sequence, sample_sequence

logger = logging.getLogger(__name__)


def dirichlet_sample_row(
    alpha_vec: np.ndarray,
    rng: np.random.Generator
) -> np.ndarray:
    """
    Sample a row from a Dirichlet distribution.

    Args:
        alpha_vec: Concentration parameters
        rng: Random number generator

    Returns:
        Sampled probability vector (sums to 1)

    Raises:
        ValueError: If any alpha parameter is negative
    """
    if np.any(alpha_vec < 0):
        raise ValueError("Alpha parameters must be non-negative")

    # Handle edge case where all alphas are zero
    if np.all(alpha_vec == 0):
        n = len(alpha_vec)
        return np.ones(n) / n

    # Sample from Dirichlet distribution
    samples = rng.gamma(alpha_vec, 1.0)
    return samples / samples.sum()


def sample_transition_matrix_posterior(
    sequences: list[list[int]],
    states: list[int],
    alpha: float = 1.0,
    rng: Optional[np.random.Generator] = None
) -> np.ndarray:
    """
    Sample a transition matrix from the posterior distribution.

    Args:
        sequences: Training sequences
        states: List of state values
        alpha: Dirichlet concentration parameter (must be > 0)
        rng: Random number generator

    Returns:
        Sampled transition matrix

    Raises:
        ValueError: If alpha <= 0 or no valid sequences
    """
    if alpha <= 0:
        raise ValueError(f"Alpha must be positive, got {alpha}")

    if not sequences:
        raise ValueError("No sequences provided")

    if rng is None:
        rng = np.random.default_rng()

    # Build transition counts
    counts, _ = build_transition_counts(sequences)

    n = len(states)
    state_to_idx = {state: i for i, state in enumerate(states)}
    trans_mat = np.zeros((n, n), dtype=np.float64)

    # Sample each row from Dirichlet posterior
    for i, state in enumerate(states):
        if state in counts:
            # Get counts for this state
            state_counts = counts[state]
            alpha_vec = np.full(n, alpha, dtype=np.float64)

            for next_state, count in state_counts.items():
                if next_state in state_to_idx:
                    j = state_to_idx[next_state]
                    alpha_vec[j] += count

            # Sample row from Dirichlet posterior
            trans_mat[i] = dirichlet_sample_row(alpha_vec, rng)
        else:
            # No transitions from this state, use uniform distribution
            trans_mat[i] = dirichlet_sample_row(np.full(n, alpha), rng)

    # Ensure numeric stability
    assert np.all(trans_mat >= 0), "Negative probabilities in sampled matrix"
    assert np.allclose(trans_mat.sum(axis=1), 1.0), "Sampled matrix rows do not sum to 1"

    logger.info(f"Sampled transition matrix from posterior with alpha={alpha}")
    return trans_mat


def posterior_predictive_sequences(
    sequences: list[list[int]],
    states: list[int],
    start_probs: np.ndarray,
    alpha: float = 1.0,
    length: int = 7,
    nsamples: int = 1000,
    rng: Optional[np.random.Generator] = None
) -> list[tuple[list[int], float]]:
    """
    Generate posterior predictive sequences.

    Args:
        sequences: Training sequences
        states: List of state values
        start_probs: Start state probabilities
        alpha: Dirichlet concentration parameter (must be > 0)
        length: Sequence length to generate
        nsamples: Number of posterior samples
        rng: Random number generator

    Returns:
        List of (sequence, log_probability) tuples

    Raises:
        ValueError: If alpha <= 0, length < 1, or nsamples < 1
    """
    if alpha <= 0:
        raise ValueError(f"Alpha must be positive, got {alpha}")

    if length < 1:
        raise ValueError(f"Length must be positive, got {length}")

    if nsamples < 1:
        raise ValueError(f"Number of samples must be positive, got {nsamples}")

    if rng is None:
        rng = np.random.default_rng()

    sims = []

    for i in range(nsamples):
        # Sample transition matrix from posterior
        trans_mat = sample_transition_matrix_posterior(
            sequences, states, alpha=alpha, rng=rng
        )

        # Sample sequence from this matrix
        seq = sample_sequence(
            states, start_probs, trans_mat, length=length, rng=rng
        )

        # Compute log probability
        lp = logprob_of_sequence(seq, states, start_probs, trans_mat)
        sims.append((seq, lp))

        if (i + 1) % 100 == 0:
            logger.info(f"Generated {i + 1}/{nsamples} posterior predictive sequences")

    logger.info(f"Generated {len(sims)} posterior predictive sequences")
    return sims
