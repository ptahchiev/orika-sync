package com.test;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
import org.reflections.Reflections;
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
    
    private Synchronizer mapper = new Synchronizer();

    @Test
    @Transactional
    public void testSync() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {

    	
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
			
			configureSync(factory, "com.db");
		}

		
		void configureSync(MapperFactory factory, String packageName) {
			
			Reflections reflections = new Reflections(packageName);

			Set<Class<? extends AbstractEntity>> subTypes = reflections.getSubTypesOf(AbstractEntity.class);
			
			for (final Class<? extends AbstractEntity> cls : subTypes) {
				if(AbstractCatalogableEntity.class.isAssignableFrom(cls)) {
					ClassMapBuilder<? extends AbstractEntity, ? extends AbstractEntity> cmb = factory.classMap(cls, cls).exclude("pk");
					// register ObjectFactory
					Type<? extends AbstractEntity> targetClass = TypeFactory.valueOf(cls);
					factory.registerObjectFactory(new AbstractCatalogableEntityFactory(cls), targetClass);
					
					cmb.exclude("catalogVersion").customize(new AbstractCatalogableEntityMapper());
					cmb.byDefault().register();
				}
			}
		}
		
    	
    };
    
    
    public  class AbstractCatalogableEntityFactory<T extends AbstractCatalogableEntity> implements ObjectFactory<T> {

    	protected Class<T> targetClass;
    	
    	
    	public AbstractCatalogableEntityFactory(Class<T> targetClass) {
			super();
			this.targetClass = targetClass;
		}


		protected  T newInstance(Object obj) {
    		try {
				return (T) targetClass.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Can not instantiate "+targetClass, e);
			}
    	}
    	
    	
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
                onlineEntity = newInstance(obj);
                onlineEntity.setCatalogVersion(onlineCatalogVersion);
                entityManager.merge(onlineEntity);
                entityManager.flush();
                return onlineEntity;
            }
        }
    }
}
