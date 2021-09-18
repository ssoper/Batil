package com.seansoper.batil.brokers.etrade

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap
import java.time.Instant

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

/**
 * Mutual Fund Quantity
 * @param [cash] The value of the cash quantity in the mutual fund
 * @param [margin] The value of the margin quantity in the mutual fund
 * @param [cusip] The CUSIP value of the mutual fund symbol
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class MutualFundQuantity(
    val cash: Float?,
    val margin: Float?,
    val cusip: String?
)

/**
 * Lot
 * @param [id] The lot ID of the lot selected to sell
 * @param [size] The number of shares to sell for the selected lot
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Lot(
    val id: Int,
    val size: Float?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LotsResponse(
    val lot: List<Lot>
)

/**
 * Instrument
 * @param [product] The product details for the security
 * @param [symbolDescription] The text description of the security being bought or sold
 * @param [orderAction] BUY, SELL, BUY_TO_COVER, SELL_SHORT, BUY_OPEN, BUY_CLOSE, SELL_OPEN, SELL_CLOSE, EXCHANGE
 * @param [quantityType] QUANTITY, DOLLAR, ALL_I_OWN
 * @param [quantity] The number of shares to buy or sell
 * @param [cancelQuantity] The number of shares to cancel ordering
 * @param [orderedQuantity] The number of shares ordered
 * @param [filledQuantity] The number of shares filled
 * @param [averageExecutionPrice] The average execution price
 * @param [estimatedCommission] The cost billed to the user to perform the requested action
 * @param [estimatedFees] The cost or proceeds, including broker commission, resulting from the requested action
 * @param [bid] The bid price
 * @param [ask] The ask price
 * @param [lastprice] The last price
 * @param [currency] USD, EUR, GBP, HKD, JPY, CAD
 * @param [lots] The object for the position lot
 * @param [mfQuantity] The object for the mutual fund quantity
 * @param [osiKey] The Options Symbology Initiative (OSI) key containing the option root symbol, expiration date, call/put indicator, and strike price
 * @param [mfTransaction] BUY, SELL
 * @param [reserveOrder] If TRUE, this is a reserve order meaning that only a limited number of shares will be publicly displayed instead of the entire order; this is done to avoid influencing other traders
 * @param [reserveQuantity] The number of shares to be publicly displayed if this is a reserve order
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Instrument(
    @JsonProperty("Product")
    val product: TransactionStrike?,
    val symbolDescription: String?,
    val orderAction: OrderActionType?,
    val quantityType: QuanityType?,
    val quantity: Float?,
    val cancelQuantity: Float?,
    val orderedQuantity: Float?,
    val filledQuantity: Float?,
    val averageExecutionPrice: Float?,
    val estimatedCommission: Float?,
    val estimatedFees: Float?,
    val bid: Float?,
    val ask: Float?,
    val lastprice: Float?,
    val currency: String?,
    val lots: LotsResponse?,
    val mfQuantity: MutualFundQuantity?,
    val osiKey: String?,
    val mfTransaction: String?,
    val reserveOrder: Boolean?,
    val reserveQuantity: Float?
)

/**
 * Order Details
 * @param [orderNumber] The numeric ID for this order in the E*TRADE system
 * @param [accountId] The numeric account ID
 * @param [previewTime] The time of the order preview
 * @param [placedTime] The time the order was placed (UTC)
 * @param [executedTime] The time the order was executed (UTC)
 * @param [orderValue] Total cost or proceeds, including commission
 * @param [status] OPEN, EXECUTED, CANCELLED, INDIVIDUAL_FILLS, CANCEL_REQUESTED, EXPIRED, REJECTED, PARTIAL, DO_NOT_EXERCISE, DONE_TRADE_EXECUTED
 * @param [orderType] EQ, OPTN, SPREADS, BUY_WRITES, BUTTERFLY, IRON_BUTTERFLY, CONDOR, IRON_CONDOR, MF, MMF
 * @param [orderTerm] GOOD_UNTIL_CANCEL, GOOD_FOR_DAY, GOOD_TILL_DATE, IMMEDIATE_OR_CANCEL, FILL_OR_KILL
 * @param [priceType] MARKET, LIMIT, STOP, STOP_LIMIT, TRAILING_STOP_CNST_BY_LOWER_TRIGGER, UPPER_TRIGGER_BY_TRAILING_STOP_CNST, TRAILING_STOP_PRCT_BY_LOWER_TRIGGER, UPPER_TRIGGER_BY_TRAILING_STOP_PRCT, TRAILING_STOP_CNST, TRAILING_STOP_PRCT, HIDDEN_STOP, HIDDEN_STOP_BY_LOWER_TRIGGER, UPPER_TRIGGER_BY_HIDDEN_STOP, NET_DEBIT, NET_CREDIT, NET_EVEN, MARKET_ON_OPEN, MARKET_ON_CLOSE, LIMIT_ON_OPEN, LIMIT_ON_CLOSE
 * @param [priceValue] The value of the price
 * @param [limitPrice] The highest price at which to buy or the lowest price at which to sell if specified in a limit order
 * @param [stopPrice] The designated boundary price for a stop order
 * @param [stopLimitPrice] The designated boundary price for a stop-limit order
 * @param [offsetType] TRAILING_STOP_CNST, TRAILING_STOP_PRCT
 * @param [offsetValue] The stop value for trailing stop price types
 * @param [marketSession] REGULAR, EXTENDED
 * @param [routingDestination] AUTO, AMEX, BOX, CBOE, ISE, NOM, NYSE, PHX
 * @param [bracketedLimitPrice] The bracketed limit price (bracketed orders are not supported in API currently)
 * @param [initialStopPrice] The initial stop price
 * @param [trailPrice] The current trailing value. For trailing stop dollar orders, this is a fixed dollar amount. For trailing stop percentage orders, this is the price reflected by the percentage selected.
 * @param [triggerPrice] The price that an advanced order will trigger. For example, if it is a $1 buy trailing stop, then the trigger price will be $1 above the last price.
 * @param [conditionPrice] For a conditional order, the price the condition is being compared against
 * @param [conditionSymbol] For a conditional order, the symbol that the condition is being compared against
 * @param [conditionType] CONTINGENT_GTE, CONTINGENT_LTE
 * @param [conditionFollowPrice] ASK, BID, LAST
 * @param [conditionSecurityType] The condition security type
 * @param [replacedByOrderId] In the event of a change order request, the order ID of the order that is replacing a prior order.
 * @param [replacesOrderId] In the event of a change order request, the order ID of the order that the new order is replacing.
 * @param [allOrNone] If TRUE, the transactions specified in the order must be executed all at once or not at all; default is FALSE
 * @param [previewId] This parameter is required and must specify the numeric preview ID from the preview and the other parameters of this request must match the parameters of the preview.
 * @param [instrument] The object for the instrument
 * @param [messages] The object for the messages
 * @param [preClearanceCode] The preclearance code
 * @param [overrideRestrictedCd] The overrides restricted code
 * @param [investmentAmount] The amount of the investment
 * @param [positionQuantity] ENTIRE_POSITION, CASH, MARGIN
 * @param [aipFlag] Indicator to identify if automated investment planning is turned on or off
 * @param [egQual] EG_QUAL_UNSPECIFIED, EG_QUAL_QUALIFIED, EG_QUAL_NOT_IN_FORCE, EG_QUAL_NOT_A_MARKET_ORDER, EG_QUAL_NOT_AN_ELIGIBLE_SECURITY, EG_QUAL_INVALID_ORDER_TYPE, EG_QUAL_SIZE_NOT_QUALIFIED, EG_QUAL_OUTSIDE_GUARANTEED_PERIOD, EG_QUAL_INELIGIBLE_GATEWAY, EG_QUAL_INELIGIBLE_DUE_TO_IPO, EG_QUAL_INELIGIBLE_DUE_TO_SELF_DIRECTED, EG_QUAL_INELIGIBLE_DUE_TO_CHANGEORDER
 * @param [reInvestOption] REINVEST, DEPOSIT, CURRENT_HOLDING
 * @param [estimatedCommission] The cost billed to the user to perform the requested action
 * @param [estimatedFees] The estimated fees
 * @param [estimatedTotalAmount] The cost or proceeds, including broker commission, resulting from the requested action
 * @param [netPrice] The net price
 * @param [netBid] The net bid
 * @param [netAsk] The net ask
 * @param [gcd] The GCD
 * @param [ratio] The ratio
 * @param [mfpriceType] The mutual fund price type
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderDetail(
    val orderNumber: Int?,
    val accountId: String?,
    val previewTime: Instant?,
    val placedTime: Instant?,
    val executedTime: Instant?,
    val orderValue: Float?,
    val status: OrderStatus?,
    val orderType: OrderType?,
    val orderTerm: OrderTerm?,
    val priceType: OrderPriceType?,
    val priceValue: String?,
    val limitPrice: Float?,
    val stopPrice: Float?,
    val stopLimitPrice: Float?,
    val offsetType: OrderOffsetType?,
    val offsetValue: Float?,
    val marketSession: MarketSession?,
    val routingDestination: RoutingDestination?,
    val bracketedLimitPrice: Float?,
    val initialStopPrice: Float?,
    val trailPrice: Float?,
    val triggerPrice: Float?,
    val conditionPrice: Float?,
    val conditionSymbol: String?,
    val conditionType: ConditionType?,
    val conditionFollowPrice: ConditionFollowPrice?,
    val conditionSecurityType: String?,
    val replacedByOrderId: Int?,
    val replacesOrderId: Int?,
    val allOrNone: Boolean?,
    val previewId: Int?,
    @JsonProperty("Instrument")
    val instrument: List<Instrument>?,
    val messages: MessagesResponse?,
    val preClearanceCode: String?,
    val overrideRestrictedCd: Int?,
    val investmentAmount: Float?,
    val positionQuantity: PositionQuantityType?,
    val aipFlag: Boolean?,
    val egQual: ExecutionGuaranteeType?,
    val reInvestOption: ReinvestOptionType?,
    val estimatedCommission: Float?,
    val estimatedFees: Float?,
    val estimatedTotalAmount: Float?,
    val netPrice: Float?,
    val netBid: Float?,
    val netAsk: Float?,
    val gcd: Int?,
    val ratio: String?,
    val mfpriceType: String?
)

/**
 * Order
 * @param [orderId] ID number assigned to this order
 * @param [details] The order details
 * @param [orderType] EQ, OPTN, SPREADS, BUY_WRITES, BUTTERFLY, IRON_BUTTERFLY, CONDOR, IRON_CONDOR, MF, MMF
 * @param [totalOrderValue] The total order value
 * @param [totalCommission] The total commission
 * @param [orderDetail] The order confirmation ID for the placed order
 * @param [events] The events in the placed order
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Order(
    val orderId: Int,
    val details: String?,
    val orderType: OrderType?,
    val totalOrderValue: Float?,
    val totalCommission: Float?,
    @JsonProperty("OrderDetail")
    val orderDetail: List<OrderDetail>?,
    val events: EventsResponse?
)

/**
 * Event
 * @param [name] UNSPECIFIED, ORDER_PLACED, SENT_TO_CMS, SENT_TO_MARKET, MARKET_SENT_ACKNOWLEDGED, CANCEL_REQUESTED, ORDER_MODIFIED, ORDER_SENT_TO_BROKER_REVIEW, SYSTEM_REJECTED, ORDER_REJECTED, ORDER_CANCELLED, CANCEL_REJECTED, ORDER_EXPIRED, ORDER_EXECUTED, ORDER_ADJUSTED, ORDER_REVERSED, REVERSE_CANCELLATION, REVERSE_EXPIRATION, OPTION_POSITION_ASSIGNED, OPEN_ORDER_ADJUSTED, CA_CANCELLED, CA_BOOKED, IPO_ALLOCATED, DONE_TRADE_EXECUTED, REJECTION_REVERSAL
 * @param [dateTime] The date and time of the order event
 * @param [orderNumber] The numeric ID for this order in the E*TRADE system
 * @param [instrument] The object for the instrument
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Event(
    val name: OrderEventType?,
    val dateTime: Int?,
    val orderNumber: Int?,
    val instrument: Instrument
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EventsResponse(
    val event: List<Event>
)

/**
 * Wraps response for an Order
 * @param [marker] Specifies the desired starting point of the set of items to return. Used for paging as described in the Notes below.
 * @param [next] The next order
 * @param [orders] The order response
 * @param [messages] The messages associated with the order
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OrdersResponse(
    val marker: String?,
    val next: String?,
    @JsonProperty("Order")
    val orders: List<Order>?,
    val messages: MessagesResponse?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrdersResponseEnvelope(
    @JsonProperty("OrdersResponse")
    val response: OrdersResponse
)

interface OrdersApi {

    @GET("/v1/accounts/{accountIdKey}/orders")
    fun list(
        @Path("accountIdKey") accountIdKey: String,
        @QueryMap options: Map<String, String>
    ): Call<OrdersResponseEnvelope>
}
