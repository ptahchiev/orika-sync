package com.test;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

import org.hibernate.Hibernate;
import org.junit.Assert;
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
import com.db.model.Price;
import com.db.model.Product;
import com.test.config.TestConfig;

@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class })
@ContextConfiguration(classes = { TestConfig.class })
@TransactionConfiguration(transactionManager = "transactionManager")
public class Synchronize2Test extends AbstractJUnit4SpringContextTests {

    @PersistenceContext(name = "entityManagerFactory")
    private EntityManager entityManager;
    
    private Synchronizer mapper;

    @Test
    @Transactional
    public void testSync() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {

    	mapper = new Synchronizer();
    	
        final Product source = entityManager.find(Product.class, 20l);

        final Product target = mapper.map(source, Product.class);

        assertNotNull(target);
        assertNotNull(target.getCatalogVersion());
        Assert.assertEquals(target.getCatalogVersion().getUid(), "Online");
        for(Price p : target.getPrices())
        	Assert.assertEquals(p.getCatalogVersion().getUid(), "Online");
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
    public class Synchronizer extends ConfigurableMapper {
    	
    	
		@Override
		protected void configure(MapperFactory factory) {
			super.configure(factory);
			
			//
			// You can look at https://github.com/ronmamo/reflections
			// to scan and register automatically all entities and AbstractCatalogableEntity
			//
			
			configureSync(factory, Product.class, Price.class, CatalogVersion.class);
		}

		
		void configureSync(MapperFactory factory, Class<? extends AbstractEntity> ...classes) {
			
			for (final Class<? extends AbstractEntity> cls : classes) {
				
				ClassMapBuilder<? extends AbstractEntity, ? extends AbstractEntity> cmb = factory.classMap(cls, cls).exclude("pk");
				
				if(AbstractCatalogableEntity.class.isAssignableFrom(cls)) {
					// register ObjectFactory
					factory.registerObjectFactory(new AbstractCatalogableEntityFactory(){

						protected AbstractCatalogableEntity newInstance() {
							try {
								return (AbstractCatalogableEntity) cls.newInstance();
							} catch (Exception e) {
								e.printStackTrace();
								throw new RuntimeException("Can not instantiate "+cls, e);
							}
						}}, TypeFactory.valueOf(cls));
					
					cmb.exclude("catalogVersion").customize(new AbstractCatalogableEntityMapper());
				}
				cmb.byDefault().register();
			}
		}
		
    	
    };
    
    
    public abstract  class AbstractCatalogableEntityFactory<T extends AbstractCatalogableEntity> implements ObjectFactory<T> {

    	
    	protected abstract T newInstance();
    	
    	
        @SuppressWarnings("unchecked")
		public T create(Object obj, MappingContext mappingContext) {

            final AbstractCatalogableEntity source = (AbstractCatalogableEntity) obj;

            final CatalogVersion onlineCatalogVersion = entityManager.find(CatalogVersion.class, 11l);

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("catalogVersion", onlineCatalogVersion);

            String findByCatalogVersionOnline = "from " + Hibernate.getClass(source).getCanonicalName() + " where catalogVersion=:catalogVersion";

            TypedQuery<AbstractCatalogableEntity> q = entityManager.createQuery(findByCatalogVersionOnline, AbstractCatalogableEntity.class);
            q.setParameter("catalogVersion", onlineCatalogVersion);

            T onlineEntity = null;
            try {
            	System.out.println("Look at online version " + obj.getClass());
            	T singleResult = (T) q.getSingleResult();
            	System.out.println("Use the available online version "+ obj.getClass());
				return singleResult;
            } catch (javax.persistence.NoResultException nre) {
            	System.out.println("Not found online version, create new one"+ obj.getClass());
                onlineEntity = newInstance();
                onlineEntity.setCatalogVersion(onlineCatalogVersion);
                entityManager.persist(onlineEntity);
                entityManager.flush();
                return onlineEntity;
            }
        }
    }
}
