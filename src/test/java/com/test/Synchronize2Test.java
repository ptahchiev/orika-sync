package com.test;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.InvocationTargetException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.ConfigurableMapper;
import ma.glasnost.orika.metadata.ClassMapBuilder;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.db.model.AbstractCatalogableEntity;
import com.db.model.AbstractEntity;
import com.db.model.CatalogVersion;
import com.db.model.Product;
import com.test.config.TestConfig;

@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class })
@ContextConfiguration(classes = { TestConfig.class })
@TransactionConfiguration(transactionManager = "transactionManager")
public class Synchronize2Test extends AbstractJUnit4SpringContextTests {

    @PersistenceContext(name = "entityManagerFactory")
    private EntityManager entityManager;
    
    private Synchronizer mapper = new Synchronizer();

    @Test
    @Transactional
    public void testSync() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {

        final Product source = entityManager.find(Product.class, 20l);

        final Product target = mapper.map(source, Product.class);

        assertNotNull(target);
        assertNotNull(target.getCatalogVersion());
    }

   

    public static class AbstractCatalogableEntityMapper<A extends AbstractCatalogableEntity> extends CustomMapper<A, A> {

        /*
         * (non-Javadoc)
         * @see ma.glasnost.orika.CustomMapper#mapAtoB(java.lang.Object, java.lang.Object, ma.glasnost.orika.MappingContext)
         */
        @Override
        public void mapAtoB(A a, A b, MappingContext context) {

            super.mapAtoB(a, b, context);
        }

        /*
         * (non-Javadoc)
         * @see ma.glasnost.orika.CustomMapper#mapBtoA(java.lang.Object, java.lang.Object, ma.glasnost.orika.MappingContext)
         */
        @Override
        public void mapBtoA(A b, A a, MappingContext context) {
            mapAtoB(a, b, context);
        }
    }
    
    
    
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static class Synchronizer extends ConfigurableMapper {
    	
    	
		@Override
		protected void configure(MapperFactory factory) {
			super.configure(factory);
			
			//
			// You can look at https://github.com/ronmamo/reflections
			// to scan and register automatically all entities and AbstractCatalogableEntity
			//
			
			configureSync(factory, Product.class, CatalogVersion.class);
		}

		
		void configureSync(MapperFactory factory, Class<? extends AbstractEntity> ...classes) {
			for (Class<? extends AbstractEntity> cls : classes) {
				ClassMapBuilder<? extends AbstractEntity, ? extends AbstractEntity> cmb = factory.classMap(cls, cls).exclude("pk");
				
				if(cls.isAssignableFrom(AbstractCatalogableEntity.class))
					cmb.exclude("catalogVersion").customize(new AbstractCatalogableEntityMapper());
				cmb.byDefault().register();
			}
		}
		
    	
    };
    
    
}
