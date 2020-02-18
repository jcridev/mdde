from datetime import datetime
from typing import Optional

from mdde_stats.service.enums import ELogAction


class LogEvent:
    """
    Log event record values
    """

    def __init__(self,
                 run_id: str = None,
                 action: int = None,
                 node_id: str = None,
                 tuple_id: str = None,
                 epoch: int = None,
                 source: str = None
                 ):
        """
        Constructor
        :param run_id: ID of the benchmark run
        :param node_id: ID of the node where the action is logged
        :param action: Action taken
        :param epoch: POSIX timestamp
        :param tuple_id: ID of the tuple involved in the action
        :param source: ID of the benchmark client performed the action
        """
        self.run_id: str = run_id
        self.node_id: str = node_id
        self.action: int = action
        self.epoch: int = epoch
        self.tuple_id: str = tuple_id
        self.source: str = source

    def from_json_obj(self, obj):
        """
        Fill in the event attributes from the JSON object or deserialized dictionary, overriding existing values.
        :param obj: Using short JSON field names:
                    self.run_id = obj.r
                    self.node_id: str = obj.n
                    self.action: int = obj.a
                    self.epoch: int = obj.ts
                    self.tuple_id: str = obj.t
                    self.source: str = obj.s
        """

        if isinstance(obj, dict):
            self.run_id = obj.get('r')
            self.node_id: str = obj.get('n')
            self.action: int = obj.get('a')
            self.epoch: int = obj.get('ts')
            self.tuple_id: str = obj.get('t')
            self.source: str = obj.get('s')
        else:
            if hasattr(obj, 'r'):
                self.run_id = obj.r
            else:
                self.run_id = None

            if hasattr(obj, 'n'):
                self.node_id: str = obj.n
            else:
                self.node_id = None

            if hasattr(obj, 'a'):
                self.action: int = obj.a
            else:
                self.action = None

            if hasattr(obj, 'ts'):
                self.epoch: int = obj.ts
            else:
                self.epoch = None

            if hasattr(obj, 't'):
                self.tuple_id: str = obj.t
            else:
                self.tuple_id = None

            if hasattr(obj, 's'):
                self.source: str = obj.s
            else:
                self.source = None

    @property
    def epoch_to_datetime(self) -> Optional[datetime]:
        if isinstance(self.epoch, (int, float, complex)):
            return datetime.fromtimestamp(self.epoch)
        else:
            return self.epoch

    @property
    def action_enum(self) -> ELogAction:
        try:
            return ELogAction(self.action)
        except:
            return ELogAction.UNKNOWN
