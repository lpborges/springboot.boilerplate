package springboot.boilerplate.v1.services;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import rx.Observable;
import rx.schedulers.Schedulers;
import springboot.boilerplate.v1.domain.Offer;

/**
*
* @author <a href="mailto:leandropg@ciandt.com">Leandro de Paula Borges</a>
*/
@Service
public class PriceService {

    private static final long[] SELLERS_ID = { 1, 2, 3 };

    public Observable<List<Offer>> getOffersByUtm(long idSku, String utm) {
        final Observable<List<Offer>> rx = Observable.create((subscriber) -> {
            try {
                final List<Offer> offers = buildOffers(idSku);
                subscriber.onNext(offers);
                subscriber.onCompleted();
            } catch (final Exception e) {
                subscriber.onError(e);
            }
        });
        return rx.subscribeOn(Schedulers.io());
    }

    private List<Offer> buildOffers(long idSku) {
        try {
            Thread.sleep(5000l);
        } catch (final InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Arrays.stream(SELLERS_ID).mapToObj(i -> {
            final Offer offer = new Offer();
            offer.setId(i);
            offer.setPrice(new BigDecimal("50"));
            offer.setSellerId(i);
            offer.setSellerName("seller_" + i);
            offer.setName("sku_"+idSku+"_offer_"+i);
            return offer;
        }).collect(Collectors.toList());
    }
}
