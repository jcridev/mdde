from mdde.core.exception import MddeError


class FragmentationError(MddeError):
    """Error creating an instance of the environment"""

    def __init__(self, message: str):
        super(FragmentationError, self).__init__(message)
