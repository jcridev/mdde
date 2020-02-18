import argparse

from mdde_stats.config import StatsProcessorConfig


def parse_args():
    parser = argparse.ArgumentParser("MDDE benchmark statistics processor")

    # Environment
    parser.add_argument("-c", "--config", type=str, default=None,
                        help="path to the configuration file")
    parser.add_argument("-d", "--data-dir", type=str, default=None,
                        help="path where working temporary must be stored")

    return parser.parse_args()


def entrypoint(config: StatsProcessorConfig, data_dir: str):
    """
    Entry point service procedure.
    :param config: Parsed configuration file
    :param data_dir: Path to folder where temporary data will be stored by the service
    """
    raise NotImplementedError


if __name__ == "__main__":
    args = parse_args()
    parsed_config = StatsProcessorConfig()
    parsed_config.read(args.config)
    entrypoint(parsed_config, args.data_dir)
