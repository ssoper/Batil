package com.seansoper.batil.brokers.etrade.api.orderPreview

import com.fasterxml.jackson.annotation.JsonProperty
import com.seansoper.batil.brokers.etrade.api.OptionType
import com.seansoper.batil.brokers.etrade.api.OrderActionType
import com.seansoper.batil.brokers.etrade.api.OrderPriceType
import com.seansoper.batil.brokers.etrade.api.OrderTerm
import com.seansoper.batil.brokers.etrade.api.OrderType
import com.seansoper.batil.brokers.etrade.api.QuantityType
import com.seansoper.batil.brokers.etrade.api.SecurityType
import com.seansoper.batil.brokers.etrade.services.MarketSession
import java.time.ZonedDateTime

interface PreviewProduct {
    val symbol: String
    val securityType: SecurityType
}

data class PreviewProductOption(
    override val symbol: String,
    override val securityType: SecurityType = SecurityType.OPTN,

    val callPut: OptionType,
    val expiryYear: String,
    val expiryMonth: String,
    val expiryDay: String,
    val strikePrice: String
) : PreviewProduct {
    constructor(symbol: String, callPut: OptionType, expiry: ZonedDateTime, strikePrice: Float) : this(
        symbol = symbol,
        callPut = callPut,
        expiryYear = expiry.year.toString(),
        expiryMonth = expiry.monthValue.toString(),
        expiryDay = expiry.dayOfMonth.toString(),
        strikePrice = strikePrice.toString()
    )
}

data class PreviewProductEquity(
    override val symbol: String,
    override val securityType: SecurityType = SecurityType.EQ,
) : PreviewProduct

interface PreviewInstrument {
    val orderAction: OrderActionType
    val product: PreviewProduct
}

data class PreviewInstrumentOption(
    override val orderAction: OrderActionType,
    @JsonProperty("Product")
    override val product: PreviewProduct,

    val orderedQuantity: String,
    val quantity: String
) : PreviewInstrument {
    constructor(orderAction: OrderActionType, quantity: Int, product: PreviewProduct) : this(
        orderAction = orderAction,
        orderedQuantity = quantity.toString(),
        quantity = quantity.toString(),
        product = product
    )
}

data class PreviewInstrumentEquity(
    override val orderAction: OrderActionType,
    @JsonProperty("Product")
    override val product: PreviewProduct,

    val quantityType: QuantityType = QuantityType.QUANTITY,
    val quantity: String
) : PreviewInstrument {
    constructor(orderAction: OrderActionType, quantity: Int, product: PreviewProduct) : this(
        orderAction = orderAction,
        quantity = quantity.toString(),
        product = product
    )
}

interface PreviewOrder {
    val allOrNone: String
    val priceType: OrderPriceType
    val orderTerm: OrderTerm
    val marketSession: MarketSession
    val instruments: List<PreviewInstrument>
}

data class PreviewOrderLimit(
    override val allOrNone: String,
    override val priceType: OrderPriceType = OrderPriceType.LIMIT,
    override val orderTerm: OrderTerm,
    override val marketSession: MarketSession,
    @JsonProperty("Instrument")
    override val instruments: List<PreviewInstrument>,

    val limitPrice: String
) : PreviewOrder {
    constructor(
        allOrNone: Boolean = false,
        orderTerm: OrderTerm = OrderTerm.default,
        marketSession: MarketSession = MarketSession.default,
        limitPrice: Float,
        instruments: List<PreviewInstrument>
    ) : this(
        allOrNone = allOrNone.toString(),
        orderTerm = orderTerm,
        marketSession = marketSession,
        limitPrice = limitPrice.toString(),
        instruments = instruments
    )
}

data class PreviewOrderMarket(
    override val allOrNone: String,
    override val priceType: OrderPriceType = OrderPriceType.MARKET,
    override val orderTerm: OrderTerm,
    override val marketSession: MarketSession,
    @JsonProperty("Instrument")
    override val instruments: List<PreviewInstrument>,

    val limitPrice: String,
    val stopPrice: String
) : PreviewOrder {
    constructor(
        allOrNone: Boolean = false,
        orderTerm: OrderTerm = OrderTerm.default,
        marketSession: MarketSession = MarketSession.default,
        limitPrice: Float,
        stopPrice: Float,
        instruments: List<PreviewInstrument>
    ) : this(
        allOrNone = allOrNone.toString(),
        orderTerm = orderTerm,
        marketSession = marketSession,
        limitPrice = limitPrice.toString(),
        stopPrice = stopPrice.toString(),
        instruments = instruments
    )
}

data class PreviewRequest(
    val orderType: OrderType,
    val clientOrderId: String,
    @JsonProperty("Order")
    val orders: List<PreviewOrder>
)
