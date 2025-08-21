"""Tests for io_utils module."""

import tempfile
from pathlib import Path

import pytest

from mcmc_random_tool.io_utils import parse_sequences_from_file


def test_parse_sequences_from_file_valid_input():
    """Test parsing valid sequence file."""
    with tempfile.NamedTemporaryFile(mode='w', delete=False) as f:
        f.write("1,2,3,4,5\n")
        f.write("6 7 8 9 10\n")
        f.write("11,12,13,14,15\n")
        temp_file = f.name

    try:
        sequences = parse_sequences_from_file(temp_file)
        assert len(sequences) == 3
        assert sequences[0] == [1, 2, 3, 4, 5]
        assert sequences[1] == [6, 7, 8, 9, 10]
        assert sequences[2] == [11, 12, 13, 14, 15]
    finally:
        Path(temp_file).unlink()


def test_parse_sequences_from_file_with_invalid_tokens():
    """Test parsing file with invalid tokens."""
    with tempfile.NamedTemporaryFile(mode='w', delete=False) as f:
        f.write("1,abc,3,4,5\n")
        f.write("6 7 def 9 10\n")
        f.write("11,12,13,14,15\n")
        temp_file = f.name

    try:
        sequences = parse_sequences_from_file(temp_file)
        assert len(sequences) == 3
        assert sequences[0] == [1, 3, 4, 5]  # abc filtered out
        assert sequences[1] == [6, 7, 9, 10]  # def filtered out
        assert sequences[2] == [11, 12, 13, 14, 15]
    finally:
        Path(temp_file).unlink()


def test_parse_sequences_from_file_with_out_of_range():
    """Test parsing file with out-of-range values."""
    with tempfile.NamedTemporaryFile(mode='w', delete=False) as f:
        f.write("1,2,3,40,5\n")  # 40 is out of range [1, 35]
        f.write("6 7 8 9 10\n")
        f.write("0,12,13,14,15\n")  # 0 is out of range [1, 35]
        temp_file = f.name

    try:
        sequences = parse_sequences_from_file(temp_file)
        assert len(sequences) == 3
        assert sequences[0] == [1, 2, 3, 5]  # 40 filtered out
        assert sequences[1] == [6, 7, 8, 9, 10]
        assert sequences[2] == [12, 13, 14, 15]  # 0 filtered out
    finally:
        Path(temp_file).unlink()


def test_parse_sequences_from_file_custom_range():
    """Test parsing with custom state range."""
    with tempfile.NamedTemporaryFile(mode='w', delete=False) as f:
        f.write("1,2,3,4,5\n")
        f.write("6 7 8 9 10\n")
        temp_file = f.name

    try:
        sequences = parse_sequences_from_file(temp_file, valid_min=5, valid_max=10)
        assert len(sequences) == 1  # Only the second sequence has values in range [5, 10]
        assert sequences[0] == [6, 7, 8, 9, 10]  # All values in range [5, 10]
    finally:
        Path(temp_file).unlink()


def test_parse_sequences_from_file_empty():
    """Test parsing empty file."""
    with tempfile.NamedTemporaryFile(mode='w', delete=False) as f:
        temp_file = f.name

    try:
        # Empty file should return empty list, not raise error
        sequences = parse_sequences_from_file(temp_file)
        assert sequences == []
    finally:
        Path(temp_file).unlink()


def test_parse_sequences_from_file_nonexistent():
    """Test parsing nonexistent file."""
    with pytest.raises(FileNotFoundError):
        parse_sequences_from_file("nonexistent_file.txt")


def test_parse_sequences_from_file_invalid_range():
    """Test parsing with invalid range."""
    with tempfile.NamedTemporaryFile(mode='w', delete=False) as f:
        f.write("1,2,3\n")
        temp_file = f.name

    try:
        with pytest.raises(ValueError, match="valid_min.*cannot be greater than valid_max"):
            parse_sequences_from_file(temp_file, valid_min=10, valid_max=5)
    finally:
        Path(temp_file).unlink()


def test_parse_sequences_from_file_negative_min():
    """Test parsing with negative minimum."""
    with tempfile.NamedTemporaryFile(mode='w', delete=False) as f:
        f.write("1,2,3\n")
        temp_file = f.name

    try:
        with pytest.raises(ValueError, match="valid_min.*must be non-negative"):
            parse_sequences_from_file(temp_file, valid_min=-1, valid_max=5)
    finally:
        Path(temp_file).unlink()
