package sa.factory.ilmibootcampsmarte.data.repository

import sa.factory.ilmibootcampsmarte.domain.repository.IlmiBootcampRepository
import sa.factory.ilmibootcampsmarte.hardware.HardwareSensorDriver
import sa.factory.ilmibootcampsmarte.model.IlmiBootcampItem
import sa.factory.ilmibootcampsmarte.model.IlmiBootcampOrder
import sa.factory.ilmibootcampsmarte.model.IlmiBootcampTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class IlmiBootcampRepositoryImpl(
    private val sensorDriver: HardwareSensorDriver
) : IlmiBootcampRepository {

    private val _catalog = MutableStateFlow<List<IlmiBootcampItem>>(listOf(
        IlmiBootcampItem("1", "دورة دراسية أسبوعية للعلوم", "دروس المكملات وبرامج المراجعة مع المدرس", 150.0, "دورة", "علوم"),
        IlmiBootcampItem("2", "مستوى 12 ساعة شهريا للعلوم", "مراجعة وتدريس يومي للمKORONA مع المدرس", 280.0, "شهري", "مراجعة"),
        IlmiBootcampItem("3", "دورة امتحان مستوى 12Atlantique", "دروس المكملات والتمارين مع المدرس", 95.0, "جديد", "امتحانات")
    ))
    override val catalogFlow: Flow<List<IlmiBootcampItem>> = _catalog.asStateFlow()

    private val _cart = MutableStateFlow<List<IlmiBootcampItem>>(emptyList())
    override val activeCart: StateFlow<List<IlmiBootcampItem>> = _cart.asStateFlow()

    override val telemetryFlow: Flow<IlmiBootcampTelemetry> = sensorDriver.observePeripheralStream()
        .map { frame ->
            IlmiBootcampTelemetry(
                peripheralName = frame.peripheralName,
                status = frame.connectionState,
                liveReading = frame.livePayload,
                batteryPct = 96,
                signalDbm = -54
            )
        }

    override fun addToCart(item: IlmiBootcampItem) {
        val current = _cart.value.toMutableList()
        current.add(item)
        _cart.value = current
    }

    override fun removeFromCart(item: IlmiBootcampItem) {
        val current = _cart.value.toMutableList()
        current.remove(item)
        _cart.value = current
    }

    override fun clearCart() {
        _cart.value = emptyList()
    }

    override suspend fun checkout(paymentMethod: String): IlmiBootcampOrder {
        val currentItems = _cart.value.toList()
        val subtotal = currentItems.sumOf { it.priceSar }
        val vat = subtotal * 0.15
        val total = subtotal + vat
        val order = IlmiBootcampOrder(
            orderId = "ORD-${System.currentTimeMillis() % 100000}",
            items = currentItems,
            subtotalSar = subtotal,
            vatSar = vat,
            totalSar = total,
            paymentMethod = paymentMethod
        )
        clearCart()
        return order
    }

    override suspend fun refreshCatalog() {
        // Offline-first sync logic
    }
}
