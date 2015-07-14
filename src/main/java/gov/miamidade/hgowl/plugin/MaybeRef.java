package gov.miamidade.hgowl.plugin;

import java.util.concurrent.Callable;

import org.hypergraphdb.util.Ref;

/**
 * <p>
 * This is a lazy reference implementation that construct an object on demand
 * based on some external data. However, it also supports the possibility of
 * failure. 
 * </p>
 * 
 * @author Borislav Iordanov
 *
 * @param <T>
 */
public class MaybeRef<T> implements Ref<T>
{
	private T value;
	private Ref<T> factory;
	private Callable<Boolean> onfailure;
	private Exception failure;
	
	public MaybeRef(Ref<T> factory, Callable<Boolean> onfailure)
	{
		this.factory = factory;
		this.onfailure = onfailure;
	}
	
	public T get()
	{
		// deal with multithreading in more efficient way if needed here...
		synchronized (factory)
		{
			while (failure == null && value == null)
			{
				try
				{
					value = factory.get();
				}
				catch (Exception ex)
				{
					if (onfailure == null)
					{
						failure = ex;
						throw ex;
					}
					else try 
					{ 
						if (!onfailure.call()) 
						{
							failure = ex;
							throw ex; 
						}
					} catch (Exception ex2) {throw new RuntimeException(ex2);}					
				}
			}
			return value;
		}
	}
}
