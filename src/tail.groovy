import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

/**
 * tail utility
 *
 * @author vadya
 */

def cli = new CliBuilder(usage: "tail [<file>]", stopAtNonOption: false)
cli.n(longOpt: 'lines', args: 1, argName: 'K', 'output last K lines')

def opts = cli.parse(args)

if (!opts) {
    System.exit(-1)
}

def numLines = Integer.valueOf(opts.lines ?: 10)

List<String> serialTail(int numLines, Closure eachLine) {
    def buf = []
    System.in.eachLine { line ->
        buf.add(line)
        if (buf.size() > numLines)
            buf.remove(0)
    }

    buf.each { eachLine(it) }
}

List<String> randomTail(int numLines, Path file, Closure eachLine) {
    def buf = []

    FileChannel fc = FileChannel.open(file, StandardOpenOption.READ)
    ByteBuffer rbuf = ByteBuffer.allocate(128)

    def decoder = StandardCharsets.UTF_8.newDecoder()
    def position = fc.size()
    while (buf.size() < numLines + 1) { // to avoid not fully read first line
        position = rbuf.capacity() > position ? 0 : position - rbuf.capacity()
        if (fc.position(position).read(rbuf) < 0)
            break;

        rbuf.flip()

        CharBuffer cbuf = decoder.decode(rbuf)
        def lines = cbuf.normalize().split('\n')

        if (!cbuf.toString().endsWith("\n")) { // fix for string which were not read fully
            def lineEnd = buf.remove(0)
            lines[-1] += lineEnd
        }

        buf.addAll(0, lines)

        rbuf.clear()
    }

    buf.drop(buf.size() - numLines).each { eachLine(it) }
}

if (opts.arguments()) {
    randomTail(numLines, Paths.get(opts.arguments()[0])) { println(it) }
} else {
    serialTail(numLines) { println(it) }
}
