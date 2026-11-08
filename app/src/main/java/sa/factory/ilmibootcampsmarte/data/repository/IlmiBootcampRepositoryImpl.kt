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
        IlmiBootcampItem("1", "حزمة المراجعة الشهرية", "12 ساعة تدريبية مع تصحيح فوري عبر القلم الذكي وتحليل الأخطاء", 150.0, "معتمد", "مراجعة شهرية"),
        IlmiBootcampItem("2", "امتحانات محاكاة تفاعلية", "اختبارات زمنية مع تصحيح آلي عبر BLE وتغذية راجعة فورية", 280.0, "فوري", "امتحانات تجريبية"),
        IlmiBootcampItem("3", "دروس القلم النشط", "تمارين تفاعلية مع استشعار ضغط القلم 4096-مستوى وتحليل الخط", 95.0, "جديد", "دروس تفاعلية")
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
