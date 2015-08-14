package springboot.boilerplate.v1.services;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rx.Observable;
import rx.schedulers.Schedulers;
import springboot.boilerplate.v1.domain.Offer;
import springboot.boilerplate.v1.domain.Product;
import springboot.boilerplate.v1.domain.ProductVariation;

/**
 *
 * @author <a href="mailto:leandropg@ciandt.com">Leandro de Paula Borges</a>
 */

@Service
public class ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

    private static final long MAIN_SELLER_ID = 1;

    private static final long[] VARIATIONS_ID = { 1, 2, 3, 4, 5 };

    private static final long[] SELLERS_ID = { 1, 2, 3 };

    @Autowired
    private PriceService priceService;

    public Observable<Product> getRxProductById(final long id) {
        final Observable<Product> rxProduct = Observable.create((subscriber) -> {
            try {
                final Product p = getProductById(id);
                subscriber.onNext(p);
                subscriber.onCompleted();
            } catch (final Exception e) {
                subscriber.onError(e);
            }
        });
        return rxProduct.subscribeOn(Schedulers.io());
    }

    public Observable<Product> getRxProductByIdAndUtm(final long id, final String utm) {

        final Observable<Product> rxProduct = getRxProductById(id);

        final Observable<Product> completeProduct = rxProduct.flatMap(p -> {
            final List<ProductVariation> variations = p.getVariations();

            return Observable.from(variations).flatMap(v -> {

                final List<Offer> offersToSetPrice = v.getOffers().stream()
                        .filter( o -> o.getSellerId() == MAIN_SELLER_ID ).collect(Collectors.toList());

                return Observable.from(offersToSetPrice).flatMap(offer -> {
                    LOGGER.info(offer.toString());
                    return priceService.getOffersByUtm(v.getId(), utm).flatMapIterable(utmOffers -> utmOffers).flatMap(utmOffer -> {
                        if(utmOffer.getSellerId().equals(offer.getSellerId())){
                            offer.setPrice(utmOffer.getPrice());
                        }
                        return Observable.just(offer);
                    });
                });
            }).last().map(ignored -> p);
        });
        return completeProduct;
    }

    public Product getProductById(final long id) {
        return buildProduct(id);
    }

    private Product buildProduct(final long id) {
        final Product product = new Product();
        product.setId(id);
        product.setName("product_" + id);
        product.setVariations(buildVariations());
        return product;
    }

    private List<ProductVariation> buildVariations() {
        return Arrays.stream(VARIATIONS_ID).mapToObj(i -> {
            final ProductVariation pv = new ProductVariation();
            pv.setId(i);
            pv.setName("variation_" + i);
            pv.setOffers(buildOffers(i));
            return pv;
        }).collect(Collectors.toList());
    }

    private List<Offer> buildOffers(long idSku) {
        return Arrays.stream(SELLERS_ID).mapToObj(i -> {
            final Offer offer = new Offer();
            offer.setId(i);
            offer.setPrice(new BigDecimal("100"));
            offer.setSellerId(i);
            offer.setSellerName("seller_" + i);
            offer.setName("sku_"+idSku+"_offer_"+i);
            return offer;
        }).collect(Collectors.toList());
    }
}
