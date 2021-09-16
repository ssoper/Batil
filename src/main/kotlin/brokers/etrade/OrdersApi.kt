package com.seansoper.batil.brokers.etrade

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

enum class OrderType {
    EQ, OPTN, SPREADS, BUY_WRITES, BUTTERFLY, IRON_BUTTERFLY, CONDOR, IRON_CONDOR, MF, MMF
}

enum class OrderTerm {
    GOOD_UNTIL_CANCEL, GOOD_FOR_DAY, GOOD_TILL_DATE, IMMEDIATE_OR_CANCEL, FILL_OR_KILL
}

enum class OrderPriceType {
    MARKET, LIMIT, STOP, STOP_LIMIT, TRAILING_STOP_CNST_BY_LOWER_TRIGGER, UPPER_TRIGGER_BY_TRAILING_STOP_CNST, TRAILING_STOP_PRCT_BY_LOWER_TRIGGER, UPPER_TRIGGER_BY_TRAILING_STOP_PRCT, TRAILING_STOP_CNST, TRAILING_STOP_PRCT, HIDDEN_STOP, HIDDEN_STOP_BY_LOWER_TRIGGER, UPPER_TRIGGER_BY_HIDDEN_STOP, NET_DEBIT, NET_CREDIT, NET_EVEN, MARKET_ON_OPEN, MARKET_ON_CLOSE, LIMIT_ON_OPEN, LIMIT_ON_CLOSE
}

enum class OrderOffsetType {
    TRAILING_STOP_CNST, TRAILING_STOP_PRCT
}

enum class RoutingDestination {
    AUTO, AMEX, BOX, CBOE, ISE, NOM, NYSE, PHX
}

enum class ConditionType {
    CONTINGENT_GTE, CONTINGENT_LTE
}

enum class ConditionFollowPrice {
    ASK, BID, LAST
}

enum class ExecutionGuaranteeType {
    EG_QUAL_UNSPECIFIED, EG_QUAL_QUALIFIED, EG_QUAL_NOT_IN_FORCE, EG_QUAL_NOT_A_MARKET_ORDER, EG_QUAL_NOT_AN_ELIGIBLE_SECURITY, EG_QUAL_INVALID_ORDER_TYPE, EG_QUAL_SIZE_NOT_QUALIFIED, EG_QUAL_OUTSIDE_GUARANTEED_PERIOD, EG_QUAL_INELIGIBLE_GATEWAY, EG_QUAL_INELIGIBLE_DUE_TO_IPO, EG_QUAL_INELIGIBLE_DUE_TO_SELF_DIRECTED, EG_QUAL_INELIGIBLE_DUE_TO_CHANGEORDER
}

enum class ReinvestOptionType {
    REINVEST, DEPOSIT, CURRENT_HOLDING
}

enum class PositionQuantityType {
    ENTIRE_POSITION, CASH, MARGIN
}

enum class OrderActionType {
    BUY, SELL, BUY_TO_COVER, SELL_SHORT, BUY_OPEN, BUY_CLOSE, SELL_OPEN, SELL_CLOSE, EXCHANGE
}

enum class QuanityType {
    QUANTITY, DOLLAR, ALL_I_OWN
}

enum class OrderEventType {
    UNSPECIFIED, ORDER_PLACED, SENT_TO_CMS, SENT_TO_MARKET, MARKET_SENT_ACKNOWLEDGED, CANCEL_REQUESTED, ORDER_MODIFIED, ORDER_SENT_TO_BROKER_REVIEW, SYSTEM_REJECTED, ORDER_REJECTED, ORDER_CANCELLED, CANCEL_REJECTED, ORDER_EXPIRED, ORDER_EXECUTED, ORDER_ADJUSTED, ORDER_REVERSED, REVERSE_CANCELLATION, REVERSE_EXPIRATION, OPTION_POSITION_ASSIGNED, OPEN_ORDER_ADJUSTED, CA_CANCELLED, CA_BOOKED, IPO_ALLOCATED, DONE_TRADE_EXECUTED, REJECTION_REVERSAL
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class MutualFundQuantity(
    val cash: Float?, // The value of the cash quantity in the mutual fund
    val margin: Float?, // The value of the margin quantity in the mutual fund
    val cusip: String?, // The CUSIP value of the mutual fund symbol
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Lot(
    val id: Int?, // The lot ID of the lot selected to sell
    val size: Float?, // The number of shares to sell for the selected lot
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LotsResponse(
    val lot: List<Lot>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Instrument(
    val product: TransactionStrike?, // The product details for the security
    val symbolDescription: String?, // The text description of the security being bought or sold
    val orderAction: OrderActionType?, // BUY, SELL, BUY_TO_COVER, SELL_SHORT, BUY_OPEN, BUY_CLOSE, SELL_OPEN, SELL_CLOSE, EXCHANGE
    val quantityType: QuanityType?, // QUANTITY, DOLLAR, ALL_I_OWN
    val quantity: Float?, // The number of shares to buy or sell
    val cancelQuantity: Float?, // The number of shares to cancel ordering
    val orderedQuantity: Float?, // The number of shares ordered
    val filledQuantity: Float?, // The number of shares filled
    val averageExecutionPrice: Float?, // The average execution price
    val estimatedCommission: Float?, // The cost billed to the user to perform the requested action
    val estimatedFees: Float?, // The cost or proceeds, including broker commission, resulting from the requested action
    val bid: Float?, // The bid price
    val ask: Float?, // The ask price
    val lastprice: Float?, // The last price
    val currency: String?, // USD, EUR, GBP, HKD, JPY, CAD
    val lots: LotsResponse, // The object for the position lot
    val mfQuantity: MutualFundQuantity?, // The object for the mutual fund quantity
    val osiKey: String?, // The Options Symbology Initiative (OSI) key containing the option root symbol, expiration date, call/put indicator, and strike price
    val mfTransaction: String?, // BUY, SELL
    val reserveOrder: Boolean?, // If TRUE, this is a reserve order meaning that only a limited number of shares will be publicly displayed instead of the entire order; this is done to avoid influencing other traders
    val reserveQuantity: Float?, // The number of shares to be publicly displayed if this is a reserve order
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderDetail(
    val orderNumber: Int?, // The numeric ID for this order in the E*TRADE system
    val accountId: String?, // The numeric account ID
    val previewTime: Int?, // The time of the order preview
    val placedTime: Int?, // The time the order was placed (UTC)
    val executedTime: Int?, // The time the order was executed (UTC)
    val orderValue: Float?, // Total cost or proceeds, including commission
    val status: OrderStatus?, // OPEN, EXECUTED, CANCELLED, INDIVIDUAL_FILLS, CANCEL_REQUESTED, EXPIRED, REJECTED, PARTIAL, DO_NOT_EXERCISE, DONE_TRADE_EXECUTED
    val orderType: OrderType?, // EQ, OPTN, SPREADS, BUY_WRITES, BUTTERFLY, IRON_BUTTERFLY, CONDOR, IRON_CONDOR, MF, MMF
    val orderTerm: OrderTerm?, // GOOD_UNTIL_CANCEL, GOOD_FOR_DAY, GOOD_TILL_DATE, IMMEDIATE_OR_CANCEL, FILL_OR_KILL
    val priceType: OrderPriceType?, // MARKET, LIMIT, STOP, STOP_LIMIT, TRAILING_STOP_CNST_BY_LOWER_TRIGGER, UPPER_TRIGGER_BY_TRAILING_STOP_CNST, TRAILING_STOP_PRCT_BY_LOWER_TRIGGER, UPPER_TRIGGER_BY_TRAILING_STOP_PRCT, TRAILING_STOP_CNST, TRAILING_STOP_PRCT, HIDDEN_STOP, HIDDEN_STOP_BY_LOWER_TRIGGER, UPPER_TRIGGER_BY_HIDDEN_STOP, NET_DEBIT, NET_CREDIT, NET_EVEN, MARKET_ON_OPEN, MARKET_ON_CLOSE, LIMIT_ON_OPEN, LIMIT_ON_CLOSE
    val priceValue: String?, // The value of the price
    val limitPrice: Float?, // The highest price at which to buy or the lowest price at which to sell if specified in a limit order
    val stopPrice: Float?, // The designated boundary price for a stop order
    val stopLimitPrice: Float?, // The designated boundary price for a stop-limit order
    val offsetType: OrderOffsetType?, // TRAILING_STOP_CNST, TRAILING_STOP_PRCT
    val offsetValue: Float?, // The stop value for trailing stop price types
    val marketSession: MarketSession?, // REGULAR, EXTENDED
    val routingDestination: RoutingDestination?, // AUTO, AMEX, BOX, CBOE, ISE, NOM, NYSE, PHX
    val bracketedLimitPrice: Float?, // The bracketed limit price (bracketed orders are not supported in API currently)
    val initialStopPrice: Float?, // The initial stop price
    val trailPrice: Float?, // The current trailing value. For trailing stop dollar orders, this is a fixed dollar amount. For trailing stop percentage orders, this is the price reflected by the percentage selected.
    val triggerPrice: Float?, // The price that an advanced order will trigger. For example, if it is a $1 buy trailing stop, then the trigger price will be $1 above the last price.
    val conditionPrice: Float?, // For a conditional order, the price the condition is being compared against
    val conditionSymbol: String?, // For a conditional order, the symbol that the condition is being compared against
    val conditionType: ConditionType?, // CONTINGENT_GTE, CONTINGENT_LTE
    val conditionFollowPrice: ConditionFollowPrice?, // ASK, BID, LAST
    val conditionSecurityType: String?, // The condition security type
    val replacedByOrderId: Int?, // In the event of a change order request, the order ID of the order that is replacing a prior order.
    val replacesOrderId: Int?, // In the event of a change order request, the order ID of the order that the new order is replacing.
    val allOrNone: Boolean?, // If TRUE, the transactions specified in the order must be executed all at once or not at all; default is FALSE
    val previewId: Int?, // This parameter is required and must specify the numeric preview ID from the preview and the other parameters of this request must match the parameters of the preview.
    val instrument: Instrument, // The object for the instrument
    val messages: MessagesResponse, // The object for the messages
    val preClearanceCode: String?, // The preclearance code
    val overrideRestrictedCd: Int?, // The overrides restricted code
    val investmentAmount: Float?, // The amount of the investment
    val positionQuantity: PositionQuantityType?, // ENTIRE_POSITION, CASH, MARGIN
    val aipFlag: Boolean?, // Indicator to identify if automated investment planning is turned on or off
    val egQual: ExecutionGuaranteeType?, // EG_QUAL_UNSPECIFIED, EG_QUAL_QUALIFIED, EG_QUAL_NOT_IN_FORCE, EG_QUAL_NOT_A_MARKET_ORDER, EG_QUAL_NOT_AN_ELIGIBLE_SECURITY, EG_QUAL_INVALID_ORDER_TYPE, EG_QUAL_SIZE_NOT_QUALIFIED, EG_QUAL_OUTSIDE_GUARANTEED_PERIOD, EG_QUAL_INELIGIBLE_GATEWAY, EG_QUAL_INELIGIBLE_DUE_TO_IPO, EG_QUAL_INELIGIBLE_DUE_TO_SELF_DIRECTED, EG_QUAL_INELIGIBLE_DUE_TO_CHANGEORDER
    val reInvestOption: ReinvestOptionType?, // REINVEST, DEPOSIT, CURRENT_HOLDING
    val estimatedCommission: Float?, // The cost billed to the user to perform the requested action
    val estimatedFees: Float?, // The estimated fees
    val estimatedTotalAmount: Float?, // The cost or proceeds, including broker commission, resulting from the requested action
    val netPrice: Float?, // The net price
    val netBid: Float?, // The net bid
    val netAsk: Float?, // The net ask
    val gcd: Int?, // The GCD
    val ratio: String?, // The ratio
    val mfpriceType: String?, // The mutual fund price type
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Order(
    val orderId: Int?, // ID number assigned to this order
    val details: String?, // The order details
    val orderType: OrderType?, // EQ, OPTN, SPREADS, BUY_WRITES, BUTTERFLY, IRON_BUTTERFLY, CONDOR, IRON_CONDOR, MF, MMF
    val totalOrderValue: Float?, // The total order value
    val totalCommission: Float?, // The total commission
    val orderDetail: OrderDetail, // The order confirmation ID for the placed order
    val events: EventsResponse, // The events in the placed order
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Event(
    val name: OrderEventType?, // UNSPECIFIED, ORDER_PLACED, SENT_TO_CMS, SENT_TO_MARKET, MARKET_SENT_ACKNOWLEDGED, CANCEL_REQUESTED, ORDER_MODIFIED, ORDER_SENT_TO_BROKER_REVIEW, SYSTEM_REJECTED, ORDER_REJECTED, ORDER_CANCELLED, CANCEL_REJECTED, ORDER_EXPIRED, ORDER_EXECUTED, ORDER_ADJUSTED, ORDER_REVERSED, REVERSE_CANCELLATION, REVERSE_EXPIRATION, OPTION_POSITION_ASSIGNED, OPEN_ORDER_ADJUSTED, CA_CANCELLED, CA_BOOKED, IPO_ALLOCATED, DONE_TRADE_EXECUTED, REJECTION_REVERSAL
    val dateTime: Int?, // The date and time of the order event
    val orderNumber: Int?, // The numeric ID for this order in the E*TRADE system
    val instrument: Instrument, // The object for the instrument
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EventsResponse(
    val event: List<Event>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrdersResponse(
    val marker: String?, // Specifies the desired starting point of the set of items to return. Used for paging as described in the Notes below.
    val next: String?, // The next order
    val order: List<Order>, // The order response
    val messages: MessagesResponse, // The messages associated with the order
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrdersResponseEnvelope(
    @JsonProperty("OrdersResponse")
    val response: OrdersResponse
)

interface OrdersApi {

    @GET("/v1/accounts/{accountIdKey}/transactions")
    fun list(
        @Path("accountIdKey") accountIdKey: String,
        @QueryMap options: Map<String, String>
    ): Call<OrdersResponseEnvelope>
}
