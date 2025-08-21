"""Tests for CLI module."""

import tempfile
from pathlib import Path

import pytest

from mcmc_random_tool.cli import main


def test_cli_help():
    """Test CLI help output."""
    with pytest.raises(SystemExit) as exc_info:
        main(["--help"])
    assert exc_info.value.code == 0


def test_cli_no_command():
    """Test CLI with no command specified."""
    result = main([])
    assert result == 0


def test_cli_invalid_states():
    """Test CLI with invalid state range."""
    result = main(["--states", "10", "5", "analyze", "-i", "nonexistent.txt"])
    assert result == 1


def test_cli_negative_min_state():
    """Test CLI with negative minimum state."""
    result = main(["--min-state", "-1", "analyze", "-i", "nonexistent.txt"])
    assert result == 1


def test_cli_analyze_command():
    """Test analyze command with valid input."""
    # Create temporary input file
    with tempfile.NamedTemporaryFile(mode='w', delete=False) as f:
        f.write("1,2,3,4,5\n")
        f.write("2,3,4,5,6\n")
        f.write("3,4,5,6,7\n")
        input_file = f.name

    try:
        # Create temporary output directory
        with tempfile.TemporaryDirectory() as temp_dir:
            result = main([
                "analyze",
                "-i", input_file,
                "--alpha", "1.0",
                "--out-prefix", f"{temp_dir}/analysis"
            ])
            assert result == 0

            # Check that output files were created
            output_files = list(Path(temp_dir).glob("analysis_*"))
            assert len(output_files) >= 4  # Should have at least 4 output files
    finally:
        Path(input_file).unlink()


def test_cli_analyze_invalid_alpha():
    """Test analyze command with invalid alpha."""
    with tempfile.NamedTemporaryFile(mode='w', delete=False) as f:
        f.write("1,2,3,4,5\n")
        input_file = f.name

    try:
        result = main([
            "analyze",
            "-i", input_file,
            "--alpha", "0.0"  # Invalid alpha
        ])
        assert result == 1
    finally:
        Path(input_file).unlink()


def test_cli_analyze_insufficient_sequences():
    """Test analyze command with insufficient sequences."""
    with tempfile.NamedTemporaryFile(mode='w', delete=False) as f:
        f.write("1,2,3,4,5\n")  # Only one sequence
        input_file = f.name

    try:
        result = main([
            "analyze",
            "-i", input_file,
            "--alpha", "1.0"
        ])
        assert result == 1
    finally:
        Path(input_file).unlink()


def test_cli_topk_command():
    """Test topk command with valid input."""
    with tempfile.NamedTemporaryFile(mode='w', delete=False) as f:
        f.write("1,2,3,4,5\n")
        f.write("2,3,4,5,6\n")
        f.write("3,4,5,6,7\n")
        input_file = f.name

    try:
        with tempfile.TemporaryDirectory() as temp_dir:
            output_file = f"{temp_dir}/topk_output.csv"
            result = main([
                "topk",
                "-i", input_file,
                "--alpha", "1.0",
                "--length", "3",
                "-n", "10",
                "-k", "5",
                "-o", output_file,
                "--seed", "42"
            ])
            assert result == 0

            # Check that output file was created
            assert Path(output_file).exists()
    finally:
        Path(input_file).unlink()


def test_cli_posterior_command():
    """Test posterior command with valid input."""
    with tempfile.NamedTemporaryFile(mode='w', delete=False) as f:
        f.write("1,2,3,4,5\n")
        f.write("2,3,4,5,6\n")
        f.write("3,4,5,6,7\n")
        input_file = f.name

    try:
        with tempfile.TemporaryDirectory() as temp_dir:
            output_file = f"{temp_dir}/posterior_output.csv"
            result = main([
                "posterior",
                "-i", input_file,
                "--alpha", "1.0",
                "--length", "3",
                "--nsamples", "10",
                "-k", "5",
                "-o", output_file,
                "--seed", "42"
            ])
            assert result == 0

            # Check that output file was created
            assert Path(output_file).exists()
    finally:
        Path(input_file).unlink()


def test_cli_predict_command():
    """Test predict command with valid input."""
    with tempfile.NamedTemporaryFile(mode='w', delete=False) as f:
        f.write("1,2,3,4,5\n")
        f.write("2,3,4,5,6\n")
        f.write("3,4,5,6,7\n")
        input_file = f.name

    try:
        result = main([
            "predict",
            "-i", input_file,
            "--alpha", "1.0",
            "--length", "3",
            "-k", "3",
            "--seed", "42"
        ])
        assert result == 0
    finally:
        Path(input_file).unlink()


def test_cli_verbose_flag():
    """Test CLI with verbose flag."""
    with tempfile.NamedTemporaryFile(mode='w', delete=False) as f:
        f.write("1,2,3,4,5\n")
        f.write("2,3,4,5,6\n")
        input_file = f.name

    try:
        # Skip this test on Windows due to matplotlib backend issues
        import sys
        if sys.platform.startswith('win'):
            pytest.skip("Skipping on Windows due to matplotlib backend issues")

        result = main([
            "--verbose",
            "analyze",
            "-i", input_file,
            "--alpha", "1.0"
        ])
        assert result == 0
    finally:
        Path(input_file).unlink()


def test_cli_states_option():
    """Test CLI with --states option."""
    with tempfile.NamedTemporaryFile(mode='w', delete=False) as f:
        f.write("1,2,3,4,5\n")
        f.write("2,3,4,5,6\n")
        input_file = f.name

    try:
        # Skip this test on Windows due to matplotlib backend issues
        import sys
        if sys.platform.startswith('win'):
            pytest.skip("Skipping on Windows due to matplotlib backend issues")

        with tempfile.TemporaryDirectory() as temp_dir:
            result = main([
                "--states", "1", "10",
                "analyze",
                "-i", input_file,
                "--alpha", "1.0",
                "--out-prefix", f"{temp_dir}/analysis"
            ])
            assert result == 0
    finally:
        Path(input_file).unlink()
