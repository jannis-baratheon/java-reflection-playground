package it.januszwisniowski.playground.proxypoc;

import org.junit.Before;
import org.junit.Test;

import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class ReflectionUtilsTest {

    private static final String SOME_STRING = "SOME STRING";
    private ReflectionUtils sut;

    @Before
    public void setUp() {
        sut = new ReflectionUtils();
    }

    @Test
    public void deferredInstance_shouldReturnInstanceInstantly() {
        // given
        Supplier instanceSupplierMock = mock(Supplier.class);

        // when
        Foo foo = sut.deferredInstance(instanceSupplierMock, Foo.class);

        // then
        assertThat(foo, is(not(nullValue())));
    }

    @Test
    public void deferredInstance_shouldNotPullInstance_whenNoMethodCalled() {
        // given
        Supplier instanceSupplierMock = mock(Supplier.class);

        // when
        sut.deferredInstance(instanceSupplierMock, Foo.class);

        // then
        verify(instanceSupplierMock, never()).get();
    }

    @Test
    public void deferredInstance_shouldPullInstance_whenVoidMethodCalled() {
        // given
        Foo mockFoo = mock(Foo.class);
        Supplier instanceSupplierMock = mock(Supplier.class);
        when(instanceSupplierMock.get()).thenReturn(mockFoo);

        // when
        Foo foo = sut.deferredInstance(instanceSupplierMock, Foo.class);
        foo.setFoo(SOME_STRING);

        // then
        verify(instanceSupplierMock, times(1)).get();
    }

    @Test
    public void deferredInstance_shouldPullInstance_whenMethodCalled() {
        // given
        Foo mockFoo = mock(Foo.class);
        Supplier instanceSupplierMock = mock(Supplier.class);
        when(instanceSupplierMock.get()).thenReturn(mockFoo);

        // when
        Foo foo = sut.deferredInstance(instanceSupplierMock, Foo.class);
        foo.getFoo();

        // then
        verify(instanceSupplierMock, times(1)).get();
    }

    @Test
    public void deferredInstance_shouldProxyCalls() {
        // given
        String testString = "a test string";
        Foo mockFoo = mock(Foo.class);
        Supplier instanceSupplierMock = mock(Supplier.class);
        when(instanceSupplierMock.get()).thenReturn(mockFoo);

        // when
        Foo foo = sut.deferredInstance(instanceSupplierMock, Foo.class);
        foo.setFoo(testString);

        // then
        verify(mockFoo, times(1)).setFoo(eq(testString));
    }

    @Test
    public void deferredInstance_shouldProxyVoidCalls() {
        // given
        Foo mockFoo = mock(Foo.class);
        Supplier instanceSupplierMock = mock(Supplier.class);
        when(instanceSupplierMock.get()).thenReturn(mockFoo);

        // when
        Foo foo = sut.deferredInstance(instanceSupplierMock, Foo.class);
        foo.getFoo();

        // then
        verify(mockFoo, times(1)).getFoo();
    }

    @Test
    public void deferredInstance_shouldPullInstanceOnlyOnce() {
        // given
        Foo mockFoo = mock(Foo.class);
        Supplier instanceSupplierMock = mock(Supplier.class);
        when(instanceSupplierMock.get()).thenReturn(mockFoo);

        // when
        Foo foo = sut.deferredInstance(instanceSupplierMock, Foo.class);
        foo.setFoo(SOME_STRING);
        foo.getFoo();
        foo.setFoo(SOME_STRING);
        foo.getFoo();

        // then
        verify(instanceSupplierMock, times(1)).get();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void stubInstance_shouldThrowUnsupportedExceptionOnAnyCall() {
        // given
        Foo foo = sut.stubInstance(SOME_STRING, Foo.class);

        // when
        foo.getFoo();

        // then
        // UnsupportedOperationException
    }

    @Test
    public void stubInstance_shouldThrowUnsupportedExceptionWithMessageOnAnyCall() {
        // given
        String exceptionMessage = "This operation is unsupported";
        Foo foo = sut.stubInstance(exceptionMessage, Foo.class);

        // when

        UnsupportedOperationException exception = null;
        try {
            foo.getFoo();
        } catch(UnsupportedOperationException e) {
            exception = e;
        }

        // then
        assertThat(exception, is(not(nullValue())));
        assertThat(exception.getMessage(), is(equalTo(exceptionMessage)));
    }

    interface Foo {

        void setFoo(String foo);

        String getFoo();
    }
}