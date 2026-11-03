package sa.factory.ilmibootcampsmarte.hardware

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

data class HardwareTelemetryFrame(
    val peripheralName: String,
    val connectionState: String,
    val livePayload: String,
    val timestamp: Long = System.currentTimeMillis()
)

class HardwareSensorDriver {
    private val peripheral = "Smart Stylus Digitizer 4096-Level + BLE Beacon"

    fun observePeripheralStream(): Flow<HardwareTelemetryFrame> = flow {
        emit(HardwareTelemetryFrame(peripheral, "CONNECTING", "Handshake initializing..."))
        delay(300)
        emit(HardwareTelemetryFrame(peripheral, "ONLINE", "Sample hardware telemetry string (e.g. 'BLE Beacon: -65 dBm | Stylus Digitizer: 4096 levels')"))

        while (true) {
            delay(1500)
            val jitter = Random.nextInt(1, 10)
            emit(HardwareTelemetryFrame(peripheral, "ACTIVE_STREAMING", "Sample hardware telemetry string (e.g. 'BLE Beacon: -65 dBm | Stylus Digitizer: 4096 levels') | Frame #${System.currentTimeMillis() % 10000} (Jitter: +${jitter}ms)"))
        }
    }
}
