package springboot.boilerplate.v1.resources;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.annotations.Api;
import rx.Observable;
import rx.Observer;
import springboot.boilerplate.v1.domain.Product;
import springboot.boilerplate.v1.services.ProductService;

/**
*
* @author <a href="mailto:leandropg@ciandt.com">Leandro de Paula Borges</a>
*/

@Path("products")
@Api(value = "products")
@Produces(MediaType.APPLICATION_JSON)
public class ProductDetailResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductDetailResource.class);

    @Autowired
    private ProductService productService;

    @GET
    @Path("/{id}")
    public void getProduct(@Suspended final AsyncResponse asyncResponse,
            @NotNull @PathParam(value="id") long id,
            @QueryParam(value="utm") String utm) {

        final long start = System.currentTimeMillis();

        final Observable<Product> product = productService.getRxProductByIdAndUtm(id, utm);

        product.subscribe(new Observer<Product>() {
             @Override
             public void onCompleted() {
             }

             @Override
             public void onError(Throwable e) {
                 asyncResponse.resume(e);
             }

             @Override
             public void onNext(Product product) {
                LOGGER.info("ProducIn={}ms", (System.currentTimeMillis() - start));
                asyncResponse.resume(product);
             }
        });
    }
}
