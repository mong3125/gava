import com.github.f4b6a3.tsid.TsidFactory
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.ThreadLocalRandom
import java.util.function.IntSupplier

object CustomTsidFactory {
    private val customEpoch = Instant.parse("2025-01-01T00:00:00Z")
    private val clock = Clock.system(ZoneId.of("Asia/Seoul"))

    val factory: TsidFactory = TsidFactory.builder()
        .withCustomEpoch(customEpoch)
        .withClock(clock)
        .withNodeBits(2)
        .withNode(0)
        .withRandomFunction(IntSupplier { ThreadLocalRandom.current().nextInt() })
        .build()
}
