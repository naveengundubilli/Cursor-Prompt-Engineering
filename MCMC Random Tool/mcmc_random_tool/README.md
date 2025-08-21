# MCMC Random Tool

A robust, production-ready Python tool for analyzing sequences using **first-order Markov chains** with **Dirichlet smoothing** and **posterior predictive sampling**.

## Features

- **Markov Chain Analysis**: Build transition matrices with Dirichlet/Laplace smoothing
- **Posterior Sampling**: Bayesian posterior predictive sequence generation
- **Monte Carlo Ranking**: Top-k sequence ranking by probability
- **Visualization**: Frequency plots and transition matrix heatmaps
- **Reproducible Results**: Deterministic sampling with seed control
- **Input Validation**: Robust error handling and validation
- **Type Safety**: Full type hints throughout the codebase

## Quick Start

### Installation

```bash
# Clone the repository
git clone https://github.com/your-org/mcmc-random-tool.git
cd mcmc-random-tool

# Install in development mode
pip install -e .

# Install development dependencies
pip install -e .[dev]
```

### Basic Usage

```bash
# Analyze sequences and generate visualizations
mcmc-random-tool analyze -i examples/input.txt --alpha 1.0 --out-prefix analysis

# Generate top-k sequences by sampling
mcmc-random-tool topk -i examples/input.txt -n 1000 -k 10 --seed 123

# Bayesian posterior predictive sampling
mcmc-random-tool posterior -i examples/input.txt --nsamples 1000 -k 10 --seed 2025
```

## Input Format

Sequences should be provided in a text file, one sequence per line. Values can be separated by commas or whitespace:

```
33,5,12,14,19,32,1
12 21 30 8 33 14 11
20,29,22,15,16,12,5
```

By default, the tool works with states in the range [1, 35]. Values outside this range are ignored.

## Commands

### 1. Analyze

Build transition matrices, start distributions, and generate visualizations:

```bash
mcmc-random-tool analyze -i input.txt --alpha 1.0 --out-prefix analysis
```

**Outputs:**
- `analysis_transition_matrix.csv` - Transition probability matrix
- `analysis_start_probs.csv` - Start state probabilities  
- `analysis_frequency.png` - Frequency distribution plot
- `analysis_heatmap.png` - Transition matrix heatmap

### 2. Predict

Generate most-likely (argmax) and sampled sequences:

```bash
mcmc-random-tool predict -i input.txt --length 7 -k 5 --seed 42
```

**Output:** JSON with argmax and sampled sequences

### 3. Top-k

Sample sequences and rank by probability:

```bash
mcmc-random-tool topk -i input.txt -n 1000 -k 10 -o results.csv --seed 123
```

**Output:** CSV file with ranked sequences and probabilities

### 4. Posterior

Bayesian posterior predictive sampling:

```bash
mcmc-random-tool posterior -i input.txt --nsamples 1000 -k 10 -o posterior.csv --seed 2025
```

**Output:** CSV file with posterior predictive rankings

## Parameters

### Alpha (Smoothing Parameter)

Controls Dirichlet/Laplace smoothing:
- `alpha = 1.0` (default): Laplace smoothing
- `alpha > 1.0`: Stronger smoothing
- `alpha < 1.0`: Weaker smoothing

### State Range

Customize the valid state range:

```bash
# Use states 1-10
mcmc-random-tool analyze -i input.txt --states 1 10

# Use states 0-9  
mcmc-random-tool analyze -i input.txt --min-state 0 --max-state 9
```

## Mathematical Background

### Dirichlet Smoothing

The tool uses Dirichlet smoothing to handle unseen transitions:

```
P(next_state | current_state) = (count + α) / (total + α × n_states)
```

Where α is the smoothing parameter and n_states is the number of possible states.

### Posterior Predictive Sampling

For Bayesian analysis, the tool samples transition matrices from the posterior distribution:

1. Build transition counts from observed sequences
2. Sample transition matrices from Dirichlet posterior per row
3. Generate sequences from sampled matrices
4. Rank sequences by mean posterior probability

## Development

### Setup

```bash
# Install development dependencies
pip install -e .[dev]

# Install pre-commit hooks
pre-commit install
```

### Quality Checks

```bash
# Run linting
ruff check .

# Run type checking
mypy mcmc_random_tool/

# Run tests
pytest

# Run all checks
pre-commit run --all-files
```

### Testing

```bash
# Run tests with coverage
pytest --cov=mcmc_random_tool

# Run specific test file
pytest tests/test_model.py

# Run tests with verbose output
pytest -v
```

## Project Structure

```
mcmc_random_tool/
├── mcmc_random_tool/
│   ├── __init__.py          # Package initialization
│   ├── cli.py              # Command-line interface
│   ├── io_utils.py         # Input/output utilities
│   ├── model.py            # Markov chain model
│   ├── posterior.py        # Posterior sampling
│   └── viz.py              # Visualization utilities
├── tests/                  # Test suite
├── examples/               # Example data
├── docs/                   # Documentation
├── pyproject.toml          # Package configuration
├── requirements.txt        # Development dependencies
├── .pre-commit-config.yaml # Pre-commit hooks
└── README.md              # This file
```

## Configuration Files

- **pyproject.toml**: Package metadata, dependencies, and tool configurations
- **.pre-commit-config.yaml**: Pre-commit hooks for code quality
- **ruff.toml**: Linting configuration (embedded in pyproject.toml)
- **mypy.ini**: Type checking configuration (embedded in pyproject.toml)

## CI/CD

The project includes GitHub Actions workflows:

- **CI**: Runs tests on Python 3.9, 3.10, 3.11
- **Quality Gates**: Ruff linting, mypy type checking, pytest
- **Build**: Package building and validation

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Run quality checks: `pre-commit run --all-files`
6. Submit a pull request

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Disclaimer

⚠️ **Important**: This tool is designed for educational and research purposes. It is **not intended for predicting gambling or lottery outcomes**. The Markov chain model is a simplified representation and should not be used for financial decision-making.

## Documentation

- [Usage Guide](docs/USAGE.md) - Detailed command reference
- [Contributing Guidelines](CONTRIBUTING.md) - How to contribute
- [Code of Conduct](CODE_OF_CONDUCT.md) - Community guidelines
- [Changelog](CHANGELOG.md) - Version history

## Support

For questions and issues:

1. Check the [Usage Guide](docs/USAGE.md)
2. Search existing [issues](https://github.com/your-org/mcmc-random-tool/issues)
3. Create a new issue with:
   - Python version
   - Operating system
   - Steps to reproduce
   - Expected vs actual behavior
