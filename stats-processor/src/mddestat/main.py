import argparse

from src.mddestat.config import StatsProcessorConfig


def parse_args():
    parser = argparse.ArgumentParser("MDDE benchmark statistics processor")

    # Environment
    parser.add_argument("-c", "--config", type=str, default=None,
                        help="path to the configuration file")

    return parser.parse_args()


def entrypoint(config: StatsProcessorConfig):
    raise NotImplementedError


if __name__ == "__main__":
    args = parse_args()
    parsed_config = StatsProcessorConfig()
    parsed_config.read(args.config)
    entrypoint(parsed_config)
