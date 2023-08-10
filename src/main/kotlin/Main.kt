import org.apache.poi.ss.formula.functions.Finance
import kotlin.math.abs
import kotlin.math.roundToInt

const val baseRateValue: Double = 13.7
const val roEValue: Double = 2.0
const val pdValue: Double = 15.8
const val lgdValue: Double = 45.0

const val rateChange: Double = 0.01
fun main(args: Array<String>) {
    println("Hello World!")

 val rate = riskRatePricingRate(rate = baseRateValue, term = 12, principal = 100000.0)
    println("Rate: $rate")

}

private fun riskRatePricingRate(rate: Double, term: Int, principal: Double): Double {
    val payment = monthlyPayment(rate = rate, term = term, principal = principal).roundToInt()
    val interests: MutableList<Int> = mutableListOf()
    var mBalance = principal

    for (i in 1..term) {
        val cof = costOfFund(baseRate = baseRateValue, mBalance)
        val coc = costOfCapital(roe = roEValue, balance = mBalance)
        val cr = creditRisk(pd = pdValue, lgd = lgdValue, balance = mBalance)
        val interest = (cof + coc + cr).roundToInt()
        mBalance = mBalance - (payment - interest)
        interests.add(interest).also {
            println("Month: $i Closing balance: $mBalance  Repayment: $payment  Interest: $interest")
        }
    }

    return if (mBalance < 0) rate else riskRatePricingRate(
        rate = rate.plus(rateChange),
        term = term,
        principal = principal
    )
}

private fun monthlyPayment(rate: Double, term: Int, principal: Double): Double =
    abs(Finance.pmt(rate.div(12 * 100), term, principal))


private fun costOfFund(baseRate: Double, balance: Double): Double = baseRate.div(12 * 100).times(balance)
private fun costOfCapital(roe: Double, balance: Double): Double = roe.div(12 * 100).times(balance)
private fun creditRisk(pd: Double, lgd: Double, balance: Double): Double {
    val mPd = pd.div(100)
    val mlgd = lgd.div(100)
    val creditRisk = balance * ((mPd * mlgd) / 12)
    return creditRisk
}