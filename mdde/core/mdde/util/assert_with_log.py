import logging


def assert_with_log(assertion_: bool, message: str = "", logger: logging.Logger = None) -> None:
    """
    Make assertion and write to log if it fails
    :param assertion_: Expression to assert
    :param message: Message to return in case of a failure
    :param logger: (optional) Logger instance to use for writing the error
    """
    try:
        assert assertion_, message
    except AssertionError as ex:
        if logger is not None:
            logger.error(ex)
        else:
            logging.error(ex)
        raise ex
