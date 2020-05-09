from enum import IntEnum


class EActionResult(IntEnum):
    """Possible return  values for action execution by an agent."""
    denied = -1
    """Action was denied by MDDE because it's not correct in the current state (ex. remove unique element)."""
    ok = 0
    """Action was performed correctly."""
    done = 1
    """Agent is done for the episode and did not perform any actions."""
    did_nothing = 255
    """Agent did nothing (for example if it skips the step but might do something in the future, unlike 'done'
    which is a terminal state for the episode)."""
