# Contributing to MCMC Random Tool

Thank you for your interest in contributing to MCMC Random Tool! This document provides guidelines for contributing to the project.

## Development Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/your-org/mcmc-random-tool.git
   cd mcmc-random-tool
   ```

2. Install the package in development mode:
   ```bash
   pip install -e .[dev]
   ```

3. Install pre-commit hooks:
   ```bash
   pre-commit install
   ```

## Code Style

We use the following tools to maintain code quality:

- **ruff**: For linting and formatting
- **mypy**: For type checking
- **pre-commit**: For automated checks

Run the quality checks:
```bash
ruff check .
mypy mcmc_random_tool/
pytest
```

## Testing

We use pytest for testing. Run the test suite:
```bash
pytest
```

For verbose output:
```bash
pytest -v
```

For coverage:
```bash
pytest --cov=mcmc_random_tool
```

## Making Changes

1. Create a new branch for your feature:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Make your changes following the coding standards

3. Add tests for new functionality

4. Run the quality checks:
   ```bash
   pre-commit run --all-files
   ```

5. Commit your changes with a descriptive message:
   ```bash
   git commit -m "Add feature: description of changes"
   ```

6. Push your branch and create a pull request

## Pull Request Guidelines

- Provide a clear description of the changes
- Include tests for new functionality
- Ensure all quality checks pass
- Update documentation if needed
- Follow the existing code style

## Reporting Issues

When reporting issues, please include:

- Python version
- Operating system
- Steps to reproduce the issue
- Expected vs actual behavior
- Error messages (if any)

## License

By contributing to this project, you agree that your contributions will be licensed under the MIT License.
