# MCMC Random Tool Usage Guide

This document provides detailed information about using the MCMC Random Tool command-line interface.

## Installation

Install the package in development mode:
```bash
pip install -e .
```

## Basic Usage

The tool provides four main commands: `analyze`, `predict`, `topk`, and `posterior`. All commands support common options for state range and smoothing parameters.

### Global Options

- `--verbose, -v`: Enable verbose logging
- `--states MIN MAX`: Set state range (overrides --min-state and --max-state)
- `--min-state`: Minimum valid state value (default: 1)
- `--max-state`: Maximum valid state value (default: 35)

## Commands

### 1. Analyze Command

Analyzes sequences and generates visualizations and data files.

```bash
mcmc-random-tool analyze -i INPUT_FILE [OPTIONS]
```

**Options:**
- `-i, --input`: Input file path with sequences (required)
- `--alpha`: Dirichlet/Laplace smoothing parameter (default: 1.0)
- `--out-prefix`: Prefix for output files (default: analysis)

**Outputs:**
- `{prefix}_transition_matrix.csv`: Transition probability matrix
- `{prefix}_start_probs.csv`: Start state probabilities
- `{prefix}_frequency.png`: Frequency distribution plot
- `{prefix}_heatmap.png`: Transition matrix heatmap

**Example:**
```bash
mcmc-random-tool analyze -i examples/input.txt --alpha 1.0 --out-prefix analysis
```

### 2. Predict Command

Generates most-likely (argmax) and sampled sequences.

```bash
mcmc-random-tool predict -i INPUT_FILE [OPTIONS]
```

**Options:**
- `-i, --input`: Input file path with sequences (required)
- `--alpha`: Dirichlet/Laplace smoothing parameter (default: 1.0)
- `--length`: Sequence length to generate (default: 7)
- `-k`: Number of argmax starts and sampled sequences (default: 5)
- `--seed`: Random seed (default: 42)

**Output:** JSON with argmax and sampled sequences

**Example:**
```bash
mcmc-random-tool predict -i examples/input.txt --length 5 -k 3 --seed 123
```

### 3. Top-k Command

Samples sequences and ranks them by probability.

```bash
mcmc-random-tool topk -i INPUT_FILE [OPTIONS]
```

**Options:**
- `-i, --input`: Input file path with sequences (required)
- `--alpha`: Dirichlet/Laplace smoothing parameter (default: 1.0)
- `--length`: Sequence length to sample (default: 7)
- `-n`: Number of samples to draw (default: 1000)
- `-k`: Show top-k in console (default: 10)
- `-o, --out`: Output CSV file (default: topk_sequences.csv)
- `--seed`: Random seed (default: 123)

**Output:** CSV file with ranked sequences and probabilities

**Example:**
```bash
mcmc-random-tool topk -i examples/input.txt -n 500 -k 5 -o results.csv --seed 456
```

### 4. Posterior Command

Performs Bayesian posterior predictive sampling.

```bash
mcmc-random-tool posterior -i INPUT_FILE [OPTIONS]
```

**Options:**
- `-i, --input`: Input file path with sequences (required)
- `--alpha`: Dirichlet concentration parameter (default: 1.0)
- `--length`: Sequence length to sample (default: 7)
- `--nsamples`: Number of posterior samples to draw (default: 1000)
- `-k`: Show top-k in console (default: 10)
- `-o, --out`: Output CSV file (default: posterior_top_sequences.csv)
- `--seed`: Random seed (default: 2025)

**Output:** CSV file with posterior predictive rankings

**Example:**
```bash
mcmc-random-tool posterior -i examples/input.txt --nsamples 500 -k 5 -o posterior.csv --seed 789
```

## Input Format

The input file should contain one sequence per line. Values can be separated by commas or whitespace.

**Example input file:**
```
33,5,12,14,19,32,1
12 21 30 8 33 14 11
20,29,22,15,16,12,5
```

**Notes:**
- Values outside the valid range are ignored
- Non-integer values are ignored
- Empty lines are skipped
- Sequences with fewer than 2 states are skipped

## Parameters

### Alpha (Smoothing Parameter)

The `--alpha` parameter controls Dirichlet/Laplace smoothing:

- `alpha = 0`: No smoothing (may cause issues with unseen transitions)
- `alpha = 1`: Laplace smoothing (add-one smoothing)
- `alpha > 1`: Stronger smoothing
- `alpha < 1`: Weaker smoothing

**Recommendations:**
- Start with `alpha = 1.0` for most applications
- Use `alpha = 0.1` for large datasets with many transitions
- Use `alpha = 10.0` for very small datasets

### State Range

By default, the tool works with states in the range [1, 35]. You can customize this:

```bash
# Use states 1-10
mcmc-random-tool analyze -i input.txt --states 1 10

# Use states 0-9
mcmc-random-tool analyze -i input.txt --min-state 0 --max-state 9
```

## Output Files

### CSV Files

**Transition Matrix CSV:**
- Rows: Current states
- Columns: Next states
- Values: Transition probabilities (rows sum to 1)

**Start Probabilities CSV:**
- `state`: State value
- `start_prob`: Probability of starting in this state

**Top-k Results CSV:**
- `sequence`: Space-separated sequence
- `count_in_samples`: Number of times this sequence appeared
- `log_prob`: Log probability of the sequence
- `probability`: Probability of the sequence

**Posterior Results CSV:**
- `sequence`: Space-separated sequence
- `count_in_nsamples`: Number of times this sequence appeared
- `mean_log_prob`: Mean log probability across posterior samples
- `mean_prob`: Mean probability across posterior samples

### Plot Files

**Frequency Plot:**
- Bar chart showing frequency of each state in the input sequences

**Heatmap:**
- Color-coded transition matrix
- Darker colors indicate higher transition probabilities

## Error Handling

The tool provides clear error messages for common issues:

- **File not found**: Check the input file path
- **No valid sequences**: Ensure the input file contains valid data
- **Invalid alpha**: Alpha must be positive
- **Invalid state range**: Min state must be ≤ max state and ≥ 0
- **Insufficient sequences**: Need at least 2 sequences for analysis

## Logging

Use the `--verbose` flag to see detailed logging information:

```bash
mcmc-random-tool analyze -i input.txt --verbose
```

This will show:
- Number of sequences parsed
- Number of invalid tokens found
- Number of out-of-range values
- Progress information for long-running operations

## Reproducibility

All commands that involve random sampling support the `--seed` parameter for reproducible results:

```bash
mcmc-random-tool topk -i input.txt --seed 42
mcmc-random-tool posterior -i input.txt --seed 42
```

Using the same seed will produce identical results across runs.

## Performance Considerations

- **Large datasets**: For datasets with many sequences, consider using smaller alpha values
- **Long sequences**: Longer sequences require more computation time
- **Many samples**: The `topk` and `posterior` commands scale linearly with the number of samples
- **Memory usage**: Large state spaces (e.g., [1, 100]) will use more memory

## Troubleshooting

**Common Issues:**

1. **"No valid sequences found"**
   - Check that your input file contains valid integer sequences
   - Verify the state range is appropriate for your data

2. **"Need at least 2 sequences"**
   - Ensure your input file has multiple sequences
   - Check that sequences have at least 2 states each

3. **"Alpha must be positive"**
   - Use a positive value for the alpha parameter

4. **"min_state cannot be greater than max_state"**
   - Check your state range parameters

**Getting Help:**

```bash
# Show general help
mcmc-random-tool --help

# Show command-specific help
mcmc-random-tool analyze --help
mcmc-random-tool topk --help
```
