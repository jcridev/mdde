class EnvironmentInitializationError(Exception):
    """Error creating an instance of the environment"""
    def __init__(self, message=None):
        super(EnvironmentInitializationError, self).__init__(message)