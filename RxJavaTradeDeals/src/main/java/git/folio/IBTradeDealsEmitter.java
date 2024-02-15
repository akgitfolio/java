package git.folio;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class IBTradeDealsEmitter {

    public static void main(String[] args) throws InterruptedException {
        TradeDealsEmitter emitter = new TradeDealsEmitter();
        TradeDealsSubscriber subscriber = new TradeDealsSubscriber();

        emitter.emitTradeDeals()
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
                .subscribe(subscriber::onTradeDeal, subscriber::onError, subscriber::onComplete);

        // Let the simulation run for 1 minute
        Thread.sleep(60000);
    }
}

class TradeDealsEmitter {
    private final Random random = new Random();
    private final String[] companies = {"Apple", "Google", "Microsoft", "Amazon", "Facebook"};
    private final String[] tradeTypes = {"BUY", "SELL"};

    public Observable<TradeDeal> emitTradeDeals() {
        return Observable.interval(100, TimeUnit.MILLISECONDS)
                .map(tick -> generateTradeDeal());
    }

    private TradeDeal generateTradeDeal() {
        String company = companies[random.nextInt(companies.length)];
        String tradeType = tradeTypes[random.nextInt(tradeTypes.length)];
        double amount = 10000 + random.nextDouble() * 990000; // Random amount between 10,000 and 1,000,000
        return new TradeDeal(company, tradeType, amount);
    }
}

class TradeDealsSubscriber {
    public void onTradeDeal(TradeDeal tradeDeal) {
        System.out.println("Received trade deal: " + tradeDeal);
    }

    public void onError(Throwable e) {
        System.err.println("Error in trade deals stream: " + e.getMessage());
    }

    public void onComplete() {
        System.out.println("Trade deals stream completed");
    }
}

class TradeDeal {
    private final String company;
    private final String tradeType;
    private final double amount;

    public TradeDeal(String company, String tradeType, double amount) {
        this.company = company;
        this.tradeType = tradeType;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return String.format("%s - %s $%.2f", company, tradeType, amount);
    }
}