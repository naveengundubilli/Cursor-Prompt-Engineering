"""Command-line interface for MCMC Random Tool."""

import argparse
import json
import logging
import sys
from collections import Counter
from typing import Optional

import numpy as np
import pandas as pd

from .io_utils import parse_sequences_from_file
from .model import (
    argmax_sequence,
    build_transition_counts,
    empirical_start_distribution,
    logprob_of_sequence,
    sample_sequence,
    transition_matrix_from_counts,
)
from .posterior import posterior_predictive_sequences
from .viz import plot_frequency, plot_heatmap

logger = logging.getLogger(__name__)


def setup_logging(verbose: bool = False) -> None:
    """Setup logging configuration."""
    level = logging.DEBUG if verbose else logging.INFO
    logging.basicConfig(
        level=level,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        datefmt='%Y-%m-%d %H:%M:%S'
    )


def cmd_analyze(args: argparse.Namespace) -> int:
    """Analyze sequences and generate visualizations."""
    try:
        sequences = parse_sequences_from_file(
            args.input,
            valid_min=args.min_state,
            valid_max=args.max_state
        )

        if len(sequences) < 2:
            logger.error("Need at least 2 sequences for analysis")
            return 1

        counts, states = build_transition_counts(sequences)
        trans = transition_matrix_from_counts(counts, states, alpha=args.alpha)
        start_probs = empirical_start_distribution(sequences, states, alpha=args.alpha)

        # Create frequency distribution
        all_numbers = [x for seq in sequences for x in seq]
        freq = pd.Series(Counter(all_numbers)).sort_index()

        # Setup output paths
        out_prefix = args.out_prefix
        freq_path = f"{out_prefix}_frequency.png"
        heat_path = f"{out_prefix}_heatmap.png"
        csv_path = f"{out_prefix}_transition_matrix.csv"
        starts_path = f"{out_prefix}_start_probs.csv"

        # Generate outputs
        plot_frequency(freq, freq_path)
        plot_heatmap(trans, states, heat_path)

        tm_df = pd.DataFrame(trans, index=states, columns=states)
        tm_df.to_csv(csv_path, index=True)

        sp_df = pd.DataFrame({"state": states, "start_prob": start_probs})
        sp_df.to_csv(starts_path, index=False)

        logger.info("Analysis complete.")
        logger.info(f"- Transition matrix CSV: {csv_path}")
        logger.info(f"- Start-state probabilities CSV: {starts_path}")
        logger.info(f"- Frequency plot: {freq_path}")
        logger.info(f"- Heatmap: {heat_path}")

        return 0

    except Exception as e:
        logger.error(f"Analysis failed: {e}")
        return 1


def cmd_predict(args: argparse.Namespace) -> int:
    """Generate predicted sequences."""
    try:
        sequences = parse_sequences_from_file(
            args.input,
            valid_min=args.min_state,
            valid_max=args.max_state
        )

        if len(sequences) < 2:
            logger.error("Need at least 2 sequences for prediction")
            return 1

        counts, states = build_transition_counts(sequences)
        trans = transition_matrix_from_counts(counts, states, alpha=args.alpha)
        start_probs = empirical_start_distribution(sequences, states, alpha=args.alpha)

        rng = np.random.default_rng(args.seed)

        # Generate argmax sequences from top starting states
        start_counts = Counter([seq[0] for seq in sequences if seq])
        top_starts = [s for s, _ in start_counts.most_common(args.k)]
        argmax_out = []

        for start_state in top_starts:
            try:
                seq = argmax_sequence(start_state, states, trans, length=args.length)
                argmax_out.append(seq)
            except ValueError as e:
                logger.warning(f"Could not generate argmax sequence for start state {start_state}: {e}")

        # Generate sampled sequences
        sampled = []
        for _ in range(args.k):
            try:
                seq = sample_sequence(states, start_probs, trans, length=args.length, rng=rng)
                sampled.append(seq)
            except Exception as e:
                logger.warning(f"Could not sample sequence: {e}")

        result = {
            "argmax_sequences": argmax_out,
            "sampled_sequences": sampled
        }
        print(json.dumps(result, indent=2))
        return 0

    except Exception as e:
        logger.error(f"Prediction failed: {e}")
        return 1


def cmd_topk(args: argparse.Namespace) -> int:
    """Generate top-k sequences by sampling."""
    try:
        sequences = parse_sequences_from_file(
            args.input,
            valid_min=args.min_state,
            valid_max=args.max_state
        )

        if len(sequences) < 2:
            logger.error("Need at least 2 sequences for top-k analysis")
            return 1

        counts, states = build_transition_counts(sequences)
        trans = transition_matrix_from_counts(counts, states, alpha=args.alpha)
        start_probs = empirical_start_distribution(sequences, states, alpha=args.alpha)

        rng = np.random.default_rng(args.seed)
        samples = []

        for i in range(args.n):
            try:
                seq = sample_sequence(states, start_probs, trans, length=args.length, rng=rng)
                lp = logprob_of_sequence(seq, states, start_probs, trans)
                samples.append((" ".join(map(str, seq)), lp))

                if (i + 1) % 100 == 0:
                    logger.info(f"Generated {i + 1}/{args.n} samples")

            except Exception as e:
                logger.warning(f"Could not sample sequence {i}: {e}")

        # Count unique sequences
        c = Counter([s for s, _ in samples])
        rows = []

        for seq_str, cnt in c.items():
            # Find log probability for this sequence
            seq_lp: Optional[float] = None
            for s2, lp2 in samples:
                if s2 == seq_str:
                    seq_lp = lp2
                    break

            rows.append({
                "sequence": seq_str,
                "count_in_samples": cnt,
                "log_prob": seq_lp,
                "probability": float(np.exp(seq_lp)) if seq_lp is not None and np.isfinite(seq_lp) else 0.0
            })

        df = pd.DataFrame(rows).sort_values(
            ["probability", "count_in_samples"],
            ascending=[False, False]
        )
        df.to_csv(args.out, index=False)

        logger.info(f"Top-k ranking saved to {args.out}")
        print(df.head(args.k).to_string(index=False))
        return 0

    except Exception as e:
        logger.error(f"Top-k analysis failed: {e}")
        return 1


def cmd_posterior(args: argparse.Namespace) -> int:
    """Generate posterior predictive sequences."""
    try:
        sequences = parse_sequences_from_file(
            args.input,
            valid_min=args.min_state,
            valid_max=args.max_state
        )

        if len(sequences) < 2:
            logger.error("Need at least 2 sequences for posterior analysis")
            return 1

        counts, states = build_transition_counts(sequences)
        start_probs = empirical_start_distribution(sequences, states, alpha=args.alpha)
        rng = np.random.default_rng(args.seed)

        sims = posterior_predictive_sequences(
            sequences, states, start_probs,
            alpha=args.alpha, length=args.length,
            nsamples=args.nsamples, rng=rng
        )

        # Count unique sequences
        seqs = [" ".join(map(str, s)) for s, _ in sims]
        c = Counter(seqs)
        rows = []

        for seq_str, cnt in c.items():
            lps = [lp for (s, lp) in sims if " ".join(map(str, s)) == seq_str]
            mean_lp = float(np.mean(lps)) if len(lps) > 0 else float("-inf")
            rows.append({
                "sequence": seq_str,
                "count_in_nsamples": cnt,
                "mean_log_prob": mean_lp,
                "mean_prob": float(np.exp(mean_lp)) if np.isfinite(mean_lp) else 0.0
            })

        df = pd.DataFrame(rows).sort_values(
            ["mean_prob", "count_in_nsamples"],
            ascending=[False, False]
        )
        df.to_csv(args.out, index=False)

        logger.info(f"Posterior predictive ranking saved to {args.out}")
        print(df.head(args.k).to_string(index=False))
        return 0

    except Exception as e:
        logger.error(f"Posterior analysis failed: {e}")
        return 1


def build_parser() -> argparse.ArgumentParser:
    """Build the command-line argument parser."""
    p = argparse.ArgumentParser(
        prog="mcmc-random-tool",
        description="Robust Markov chain analysis with Dirichlet smoothing and posterior sampling for sequences on {1..35}.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  # Analyze sequences and generate plots
  mcmc-random-tool analyze -i input.txt --alpha 1.0 --out-prefix analysis
  
  # Generate top-k sequences
  mcmc-random-tool topk -i input.txt -n 1000 -k 10 --seed 123
  
  # Posterior predictive sampling
  mcmc-random-tool posterior -i input.txt --nsamples 1000 -k 10 --seed 2025
        """
    )

    # Global options
    p.add_argument(
        "--verbose", "-v",
        action="store_true",
        help="Enable verbose logging"
    )
    p.add_argument(
        "--states",
        nargs=2, type=int, metavar=("MIN", "MAX"),
        help="State range (min max) - overrides --min-state and --max-state"
    )
    p.add_argument(
        "--min-state", type=int, default=1,
        help="Minimum valid state value (default: 1)"
    )
    p.add_argument(
        "--max-state", type=int, default=35,
        help="Maximum valid state value (default: 35)"
    )

    sub = p.add_subparsers(dest="command", help="Available commands")

    # Analyze command
    sp = sub.add_parser(
        "analyze",
        help="Analyze sequences: transition matrix, start probs, plots"
    )
    sp.add_argument(
        "-i", "--input", required=True,
        help="Input file path with sequences"
    )
    sp.add_argument(
        "--alpha", type=float, default=1.0,
        help="Dirichlet/Laplace smoothing parameter (default: 1.0)"
    )
    sp.add_argument(
        "--out-prefix", default="analysis",
        help="Prefix for output files (default: analysis)"
    )

    # Predict command
    sp = sub.add_parser(
        "predict",
        help="Generate argmax and sampled sequences from the fitted model"
    )
    sp.add_argument(
        "-i", "--input", required=True,
        help="Input file path with sequences"
    )
    sp.add_argument(
        "--alpha", type=float, default=1.0,
        help="Dirichlet/Laplace smoothing parameter (default: 1.0)"
    )
    sp.add_argument(
        "--length", type=int, default=7,
        help="Sequence length to generate (default: 7)"
    )
    sp.add_argument(
        "-k", type=int, default=5,
        help="Number of argmax starts and sampled sequences (default: 5)"
    )
    sp.add_argument(
        "--seed", type=int, default=42,
        help="Random seed (default: 42)"
    )

    # Top-k command
    sp = sub.add_parser(
        "topk",
        help="Sample N sequences and rank unique sequences by probability"
    )
    sp.add_argument(
        "-i", "--input", required=True,
        help="Input file path with sequences"
    )
    sp.add_argument(
        "--alpha", type=float, default=1.0,
        help="Dirichlet/Laplace smoothing parameter (default: 1.0)"
    )
    sp.add_argument(
        "--length", type=int, default=7,
        help="Sequence length to sample (default: 7)"
    )
    sp.add_argument(
        "-n", type=int, default=1000,
        help="Number of samples to draw (default: 1000)"
    )
    sp.add_argument(
        "-k", type=int, default=10,
        help="Show top-k in console (default: 10)"
    )
    sp.add_argument(
        "-o", "--out", default="topk_sequences.csv",
        help="Output CSV file (default: topk_sequences.csv)"
    )
    sp.add_argument(
        "--seed", type=int, default=123,
        help="Random seed (default: 123)"
    )

    # Posterior command
    sp = sub.add_parser(
        "posterior",
        help="Posterior predictive: resample transition matrices and rank sequences"
    )
    sp.add_argument(
        "-i", "--input", required=True,
        help="Input file path with sequences"
    )
    sp.add_argument(
        "--alpha", type=float, default=1.0,
        help="Dirichlet concentration parameter (default: 1.0)"
    )
    sp.add_argument(
        "--length", type=int, default=7,
        help="Sequence length to sample (default: 7)"
    )
    sp.add_argument(
        "--nsamples", type=int, default=1000,
        help="Number of posterior samples to draw (default: 1000)"
    )
    sp.add_argument(
        "-k", type=int, default=10,
        help="Show top-k in console (default: 10)"
    )
    sp.add_argument(
        "-o", "--out", default="posterior_top_sequences.csv",
        help="Output CSV file (default: posterior_top_sequences.csv)"
    )
    sp.add_argument(
        "--seed", type=int, default=2025,
        help="Random seed (default: 2025)"
    )

    return p


def main(argv: Optional[list[str]] = None) -> int:
    """Main entry point."""
    if argv is None:
        argv = sys.argv[1:]

    p = build_parser()
    args = p.parse_args(argv)

    # Setup logging
    setup_logging(args.verbose)

    # Handle --states option
    if args.states:
        args.min_state, args.max_state = args.states

    # Validate state range
    if args.min_state > args.max_state:
        logger.error(f"min_state ({args.min_state}) cannot be greater than max_state ({args.max_state})")
        return 1

    if args.min_state < 0:
        logger.error(f"min_state ({args.min_state}) must be non-negative")
        return 1

    # Validate alpha for commands that use it
    if hasattr(args, 'alpha') and args.alpha <= 0:
        logger.error(f"Alpha must be positive, got {args.alpha}")
        return 1

    # Route to appropriate command
    if args.command == "analyze":
        return cmd_analyze(args)
    elif args.command == "predict":
        return cmd_predict(args)
    elif args.command == "topk":
        return cmd_topk(args)
    elif args.command == "posterior":
        return cmd_posterior(args)
    else:
        p.print_help()
        return 0


if __name__ == "__main__":
    raise SystemExit(main())
