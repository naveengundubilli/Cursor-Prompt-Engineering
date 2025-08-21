# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2024-01-01

### Added
- Initial release of MCMC Random Tool
- Markov chain analysis with Dirichlet smoothing
- Posterior predictive sampling
- Command-line interface with subcommands: analyze, predict, topk, posterior
- Visualization utilities (frequency plots and heatmaps)
- Comprehensive input validation and error handling
- Type hints throughout the codebase
- Logging system with configurable verbosity
- Reproducible random number generation with seed support
- Package configuration with setuptools
- Development tools: pytest, ruff, mypy, pre-commit

### Features
- **analyze**: Generate transition matrices, start distributions, and visualizations
- **predict**: Generate most-likely and sampled sequences
- **topk**: Rank sequences by probability through Monte Carlo sampling
- **posterior**: Bayesian posterior predictive sequence ranking
- Support for custom state ranges via `--states` option
- Configurable Dirichlet smoothing parameter (`--alpha`)
- Output formats: CSV files and PNG plots
