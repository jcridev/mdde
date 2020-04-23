from enum import IntEnum


class EActionResult(IntEnum):
    """Possible return  values for action execution by an agent"""
    denied = -1  # Action was denied by MDDE because it's not correct in the current state (ex. remove unique element)
    ok = 0  # Action was performed correctly
    done = 1  # Agent is done
    did_nothing = 255  # Agent did nothing at all
