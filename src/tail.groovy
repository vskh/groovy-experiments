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

def numLines = Integer.valueOf(opts.lines) ?: 10

List<String> serialTail(int numLines) {
    def buf = []
    System.in.eachLine { line ->
        buf.add(line)
        if (buf.size() > numLines)
            buf.remove(0)
    }

    buf
}

List<String> randomTail(int numLines, Path file) {
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

        if (cbuf.toString().endsWith("\n")) {
            buf.addAll(0, cbuf.tokenize('\n'))
        } else {
            def lineEnd = buf.remove(0)
            def lines = cbuf.normalize().split('\n')
            lines[-1] += lineEnd
            buf.addAll(0, lines)
        }

        rbuf.clear()
    }

    buf.drop(buf.size() - numLines)
}

if (!opts.arguments()) {
    print(serialTail(numLines).join("\n"))
} else {
    print(randomTail(numLines, Paths.get(opts.arguments()[0])).join("\n"))
}
