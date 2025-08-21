"""Visualization utilities for Markov chain analysis."""

import logging

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

logger = logging.getLogger(__name__)


def plot_frequency(
    counts_series: pd.Series,
    outpath: str
) -> None:
    """
    Plot frequency distribution of states.

    Args:
        counts_series: Pandas Series with state counts
        outpath: Output file path
    """
    plt.figure(figsize=(12, 6))
    counts_series.plot(kind='bar')
    plt.title('State Frequency Distribution')
    plt.xlabel('State')
    plt.ylabel('Frequency')
    plt.xticks(rotation=45)
    plt.tight_layout()
    plt.savefig(outpath, dpi=300, bbox_inches='tight')
    plt.close()
    logger.info(f"Saved frequency plot to {outpath}")


def plot_heatmap(
    matrix: np.ndarray,
    states: list[int],
    outpath: str
) -> None:
    """
    Plot transition matrix as a heatmap.

    Args:
        matrix: Transition matrix
        states: List of state values
        outpath: Output file path
    """
    plt.figure(figsize=(10, 8))
    im = plt.imshow(matrix, cmap='Blues', aspect='auto')
    plt.colorbar(im, label='Transition Probability')

    # Set ticks
    plt.xticks(range(len(states)), [str(s) for s in states])
    plt.yticks(range(len(states)), [str(s) for s in states])
    plt.xlabel('Next State')
    plt.ylabel('Current State')
    plt.title('Transition Matrix Heatmap')

    # Add text annotations
    for i in range(len(states)):
        for j in range(len(states)):
            plt.text(j, i, f'{matrix[i, j]:.3f}',
                    ha="center", va="center", color="black" if matrix[i, j] < 0.5 else "white")

    plt.tight_layout()
    plt.savefig(outpath, dpi=300, bbox_inches='tight')
    plt.close()
    logger.info(f"Saved heatmap to {outpath}")
