class ActorInitializationError(Exception):
    """Error of creating an instance of an actor"""
    def __init__(self, message=None):
        super(ActorInitializationError, self).__init__(message)
