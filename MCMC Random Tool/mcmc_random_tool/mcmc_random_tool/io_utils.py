"""Input/output utilities for parsing sequence files."""

import logging
import re
from pathlib import Path

logger = logging.getLogger(__name__)


def parse_sequences_from_file(
    path: str,
    valid_min: int = 1,
    valid_max: int = 35
) -> list[list[int]]:
    """
    Parse sequences from a file with validation.

    Args:
        path: Path to input file
        valid_min: Minimum valid state value
        valid_max: Maximum valid state value

    Returns:
        List of sequences, where each sequence is a list of integers

    Raises:
        FileNotFoundError: If input file doesn't exist
        ValueError: If valid_min > valid_max or valid_min < 0
    """
    if valid_min > valid_max:
        raise ValueError(f"valid_min ({valid_min}) cannot be greater than valid_max ({valid_max})")
    if valid_min < 0:
        raise ValueError(f"valid_min ({valid_min}) must be non-negative")

    file_path = Path(path)
    if not file_path.exists():
        raise FileNotFoundError(f"Input file not found: {path}")

    if file_path.stat().st_size == 0:
        logger.warning("Input file is empty")
        return []

    sequences: list[list[int]] = []
    invalid_tokens = 0
    out_of_range = 0

    try:
        with open(file_path, encoding='utf-8') as f:
            for line_num, line in enumerate(f, 1):
                line = line.strip()
                if not line:
                    continue

                tokens = re.split(r"[,\s]+", line)
                seq: list[int] = []

                for token in tokens:
                    try:
                        value = int(token)
                        if valid_min <= value <= valid_max:
                            seq.append(value)
                        else:
                            out_of_range += 1
                            logger.debug(f"Line {line_num}: Value {value} out of range [{valid_min}, {valid_max}]")
                    except ValueError:
                        invalid_tokens += 1
                        logger.debug(f"Line {line_num}: Invalid token '{token}'")

                if len(seq) >= 2:  # Only keep sequences with at least 2 states
                    sequences.append(seq)
                else:
                    logger.debug(f"Line {line_num}: Sequence too short, skipping")

    except UnicodeDecodeError as e:
        raise ValueError(f"File encoding error: {e}") from e

    if invalid_tokens > 0:
        logger.info(f"Found {invalid_tokens} invalid tokens")
    if out_of_range > 0:
        logger.info(f"Found {out_of_range} values out of range [{valid_min}, {valid_max}]")

    if not sequences:
        logger.error("No valid sequences found in input file")
        raise ValueError("No valid sequences found in input file")

    logger.info(f"Parsed {len(sequences)} sequences from {path}")
    return sequences
