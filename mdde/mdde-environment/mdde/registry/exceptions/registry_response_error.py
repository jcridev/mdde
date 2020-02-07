class RegistryResponseError(Exception):
    """Error of creating an instance of an actor"""
    def __init__(self, message=None):
        super(RegistryResponseError, self).__init__(message)
