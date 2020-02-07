class FragmentationError(Exception):
    """Error creating an instance of the environment"""

    def __init__(self, message: str):
        super(FragmentationError, self).__init__(message)
