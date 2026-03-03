package com.khanabooklite.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khanabooklite.app.data.local.entity.*
import com.khanabooklite.app.data.local.relation.BillWithItems
import com.khanabooklite.app.data.repository.BillRepository
import com.khanabooklite.app.data.repository.RestaurantRepository
import com.khanabooklite.app.data.repository.MenuRepository
import com.khanabooklite.app.data.repository.InventoryRepository
import com.khanabooklite.app.domain.manager.BillCalculator
import com.khanabooklite.app.domain.manager.OrderIdManager
import com.khanabooklite.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val billRepository: BillRepository,
    private val menuRepository: MenuRepository,
    private val restaurantRepository: RestaurantRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    private val _customerName = MutableStateFlow("")
    val customerName: StateFlow<String> = _customerName

    private val _customerWhatsapp = MutableStateFlow("")
    val customerWhatsapp: StateFlow<String> = _customerWhatsapp

    private val _paymentMode = MutableStateFlow(PaymentMode.UPI)
    val paymentMode: StateFlow<PaymentMode> = _paymentMode

    private val _partAmount1 = MutableStateFlow(0.0)
    private val _partAmount2 = MutableStateFlow(0.0)

    private val _billSummary = MutableStateFlow(BillSummary())
    val billSummary: StateFlow<BillSummary> = _billSummary
    
    private val _lastBill = MutableStateFlow<BillWithItems?>(null)
    val lastBill: StateFlow<BillWithItems?> = _lastBill

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun addToCart(item: MenuItemEntity, variant: ItemVariantEntity? = null) {
        viewModelScope.launch {
            val latestItem = menuRepository.getItemById(item.id) ?: item
            val current = _cartItems.value.toMutableList()
            val existing = current.find { it.item.id == item.id && it.variant?.id == variant?.id }
            
            val currentQuantityInCart = existing?.quantity ?: 0
            
            if (currentQuantityInCart >= latestItem.stockQuantity) {
                _error.value = "Reached maximum stock for ${latestItem.name}"
                return@launch
            }

            // Show warning if reaching or below threshold
            val remainingAfterAdd = latestItem.stockQuantity - (currentQuantityInCart + 1)
            if (remainingAfterAdd <= latestItem.lowStockThreshold && remainingAfterAdd > 0) {
                _error.value = "Running out of stock for ${latestItem.name}"
            } else if (remainingAfterAdd == 0) {
                _error.value = "Reached maximum stock for ${latestItem.name}"
            }

            if (existing != null) {
                val index = current.indexOf(existing)
                current[index] = existing.copy(quantity = existing.quantity + 1)
            } else {
                current.add(CartItem(latestItem, variant, 1))
            }
            _cartItems.value = current
            updateSummary()
        }
    }

    fun removeFromCart(item: MenuItemEntity, variant: ItemVariantEntity? = null) {
        val current = _cartItems.value.toMutableList()
        val existing = current.find { it.item.id == item.id && it.variant?.id == variant?.id }
        if (existing != null) {
            val index = current.indexOf(existing)
            if (existing.quantity > 1) {
                current[index] = existing.copy(quantity = existing.quantity - 1)
            } else {
                current.removeAt(index)
            }
        }
        _cartItems.value = current
        updateSummary()
    }

    private fun updateSummary() {
        viewModelScope.launch {
            val profile = restaurantRepository.getProfile()
            val subtotal = BillCalculator.calculateSubtotal(_cartItems.value.map { 
                (it.variant?.price ?: it.item.basePrice) to it.quantity 
            })
            
            var cgst = 0.0
            var sgst = 0.0
            var customTax = 0.0
            
            if (profile?.gstEnabled == true) {
                val gst = BillCalculator.calculateGST(subtotal, profile.gstPercentage)
                cgst = gst.cgst
                sgst = gst.sgst
            } else if (profile?.customTaxPercentage != null && profile.customTaxPercentage > 0) {
                customTax = BillCalculator.calculateCustomTax(subtotal, profile.customTaxPercentage)
            }

            val total = BillCalculator.calculateTotal(subtotal, cgst, sgst, customTax)
            
            _billSummary.value = BillSummary(subtotal, cgst, sgst, customTax, total)
        }
    }

    fun setCustomerInfo(name: String, whatsapp: String) {
        _customerName.value = name
        _customerWhatsapp.value = whatsapp
    }

    fun setPaymentMode(mode: PaymentMode, p1: Double = 0.0, p2: Double = 0.0) {
        _paymentMode.value = mode
        _partAmount1.value = p1
        _partAmount2.value = p2
    }

    suspend fun completeOrder(status: PaymentStatus): Boolean {
        try {
            if (status == PaymentStatus.SUCCESS) {
                // Final stock check before confirming payment
                for (cartItem in _cartItems.value) {
                    val latestItem = menuRepository.getItemById(cartItem.item.id)
                    if (latestItem == null || latestItem.stockQuantity < cartItem.quantity) {
                        _error.value = "Insufficient stock for ${cartItem.item.name}. Available: ${latestItem?.stockQuantity ?: 0}"
                        return false
                    }
                }
            }

            val profile = restaurantRepository.getProfile() ?: return false
            val today = OrderIdManager.getTodayString()
            val dailyCounter = OrderIdManager.getNextDailyCounter(profile, today)
            val lifetimeId = OrderIdManager.getNextLifetimeId(profile)
            val displayId = OrderIdManager.getDailyOrderDisplay(today, dailyCounter)
            
            val bill = BillEntity(
                dailyOrderId = dailyCounter,
                dailyOrderDisplay = displayId,
                lifetimeOrderId = lifetimeId,
                orderType = "order",
                customerName = _customerName.value.ifBlank { null },
                customerWhatsapp = _customerWhatsapp.value.ifBlank { null },
                subtotal = _billSummary.value.subtotal,
                gstPercentage = profile.gstPercentage,
                cgstAmount = _billSummary.value.cgst,
                sgstAmount = _billSummary.value.sgst,
                customTaxAmount = _billSummary.value.customTax,
                totalAmount = _billSummary.value.total,
                paymentMode = _paymentMode.value.dbValue,
                partAmount1 = _partAmount1.value,
                partAmount2 = _partAmount2.value,
                paymentStatus = status.dbValue,
                orderStatus = if (status == PaymentStatus.SUCCESS) OrderStatus.COMPLETED.dbValue else OrderStatus.CANCELLED.dbValue,
                createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                paidAt = if (status == PaymentStatus.SUCCESS) SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()) else null
            )
            
            val items = _cartItems.value.map { cartItem ->
                BillItemEntity(
                    billId = 0,
                    menuItemId = cartItem.item.id,
                    itemName = cartItem.item.name,
                    variantId = cartItem.variant?.id,
                    variantName = cartItem.variant?.variantName,
                    price = cartItem.variant?.price ?: cartItem.item.basePrice,
                    quantity = cartItem.quantity,
                    itemTotal = (cartItem.variant?.price ?: cartItem.item.basePrice) * cartItem.quantity
                )
            }

            val payments = when (_paymentMode.value) {
                PaymentMode.PART_CASH_UPI -> listOf(
                    BillPaymentEntity(billId = 0, paymentMode = PaymentMode.CASH.dbValue, amount = _partAmount1.value),
                    BillPaymentEntity(billId = 0, paymentMode = PaymentMode.UPI.dbValue, amount = _partAmount2.value)
                )
                PaymentMode.PART_CASH_POS -> listOf(
                    BillPaymentEntity(billId = 0, paymentMode = PaymentMode.CASH.dbValue, amount = _partAmount1.value),
                    BillPaymentEntity(billId = 0, paymentMode = PaymentMode.POS.dbValue, amount = _partAmount2.value)
                )
                PaymentMode.PART_UPI_POS -> listOf(
                    BillPaymentEntity(billId = 0, paymentMode = PaymentMode.UPI.dbValue, amount = _partAmount1.value),
                    BillPaymentEntity(billId = 0, paymentMode = PaymentMode.POS.dbValue, amount = _partAmount2.value)
                )
                else -> listOf(
                    BillPaymentEntity(billId = 0, paymentMode = _paymentMode.value.dbValue, amount = _billSummary.value.total)
                )
            }
            
            billRepository.insertFullBill(bill, items, payments)
            val inserted = billRepository.getBillWithItemsByLifetimeId(lifetimeId)
            _lastBill.value = inserted
            
            if (status == PaymentStatus.SUCCESS) {
                _cartItems.value.forEach { cartItem ->
                    inventoryRepository.adjustStock(cartItem.item.id, -cartItem.quantity, "sale")
                }
            }
            
            if (profile.lastResetDate != today) restaurantRepository.resetDailyCounter(dailyCounter, today)
            else restaurantRepository.incrementOrderCounters()
            
            _cartItems.value = emptyList()
            updateSummary()
            _error.value = null
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            _error.value = "Failed to save bill: ${e.message}"
            return false
        }
    }

    fun clearError() {
        _error.value = null
    }

    data class CartItem(val item: MenuItemEntity, val variant: ItemVariantEntity? = null, val quantity: Int)
    data class BillSummary(val subtotal: Double = 0.0, val cgst: Double = 0.0, val sgst: Double = 0.0, val customTax: Double = 0.0, val total: Double = 0.0)
}
