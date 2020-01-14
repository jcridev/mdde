package dev.jcri.mdde.registry.benchmark.ycsb.cli;

import java.util.Scanner;

/**
 * Parse YCSB output that can potentially be interesting for us
 */
public class YCSBOutputParser {
    /**
     * Process the output text of YCSB.
     * Read line by line.
     * @param ycsbOut Whatever YCSB returns to its output
     * @return YCSBOutput
     */
    public YCSBOutput parse(String ycsbOut){
        var result = new YCSBOutput();
        try(Scanner scanner = new Scanner(ycsbOut)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                // Skip non relevant output
                if(!line.startsWith("[")){
                    continue;
                }
                String[] lineComponents = line.split(",", 3);
                if(lineComponents.length != 3){
                    continue;
                }
                // Top level key
                var firstLevelKeyString = lineComponents[0].replaceAll("[\\[\\]]", "");
                var firstLevelKey = EKnownYCSBLines.getCommandTag(firstLevelKeyString);
                if(firstLevelKey == null){
                    // Unknown line
                    continue;
                }
                var secondLevelModifier = firstLevelKey.tryToGetModifier(lineComponents[1].trim());
                if(secondLevelModifier == null){
                    // Unknown modifier
                    continue;
                }
                var valueString = lineComponents[2];
                var value = Double.valueOf(valueString);

                secondLevelModifier.getSetter().accept(result,value);
            }
        }
        return result;
    }
}
