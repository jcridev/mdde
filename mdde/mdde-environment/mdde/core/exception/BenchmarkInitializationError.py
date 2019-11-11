class BenchmarkInitializationError(Exception):
    """Error initializing the environment"""
    def __init__(self, message=None):
        super(BenchmarkInitializationError, self).__init__(message)