package org.bricolages.streaming.log;
package org.bricolages.streaming.event.SQSQueue;
package org.bricolages.streaming.event.SQSException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class LogQueue {
    final SQSQueue queue;

    public void send(LogEvent e) {
        queue.sendMessage(e.messageBody());
    }
}
