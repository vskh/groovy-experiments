import java.nio.ByteBuffer
import java.nio.channels.ByteChannel
import java.nio.channels.Channel
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

/**
 * tail UNIX utility implementation in Groovy
 *
 * @author vadya
 */

def cli = new CliBuilder(usage: "tail [<file>]", stopAtNonOption: false)
cli.n(longOpt: 'lines', args: 1, argName: 'K', 'output last K lines')

def opts = cli.parse(args)

if (!opts) {
    System.exit(-1)
}

def numLines = opts.lines ?: 10

List<String> serialTail(int numLines) {
    def buf = []
    System.in.eachLine { line ->
        buf.add(line)
        if (buf.size() > numLines)
            buf.remove(0)
    }

    buf
}

if (!opts.arguments()) {
    print(serialTail(numLines).join("\n"))
}
