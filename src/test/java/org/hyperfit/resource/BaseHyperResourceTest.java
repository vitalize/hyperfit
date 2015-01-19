package org.hyperfit.resource;


import org.hyperfit.exception.HyperClientException;
import org.junit.Test;

import java.util.*;

import static org.hyperfit.TestHelpers.makeSet;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * When building your own resource type you can copy paste this to your own test class
 * to get a good base going
 *
 * These tests exercise all the methods that depend upon the getLinks(String profile) implementation
 */
public class BaseHyperResourceTest {

    //Some helpers
    HyperLink makeLink(String rel){
        return makeLink(rel, null);
    }

    HyperLink makeLink(String rel, String name){
        return new HyperLink("http://host/" + UUID.randomUUID().toString(), rel, false, null, null, name, null, null, null);
    }

// BEGIN getLink(String profile) tests

    @Test(expected = HyperResourceException.class)
    public void testGetLinkByRelZeroLinksForRel() {
        String rel = UUID.randomUUID().toString();
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[0]).when(resource).getLinks(rel);

        resource.getLink(rel);
    }

    @Test
    public void testGetLinkByRelSingleLinkForRel() {
        String rel = UUID.randomUUID().toString();

        HyperLink link = makeLink(rel);
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{link}).when(resource).getLinks(rel);

        HyperLink result = resource.getLink(rel);
        assertEquals(link, result);
    }

    @Test(expected = HyperResourceException.class)
    public void testGetLinkByRelTwoLinksForRel() {
        String rel = UUID.randomUUID().toString();

        HyperLink link1 = makeLink(rel);
        HyperLink link2 = makeLink(rel);
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{link2, link1}).when(resource).getLinks(rel);

        resource.getLink(rel);

    }


// BEGIN hasLink(String profile) tests
    @Test
    public void testHasLinkByRelZeroLinksForRel() {
        String rel = UUID.randomUUID().toString();
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[0]).when(resource).getLinks(rel);

        assertFalse(resource.hasLink(rel));
    }

    @Test
    public void testHasLinkByRelSingleLinkForRel() {
        String rel = UUID.randomUUID().toString();

        HyperLink link = makeLink(rel);
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{link}).when(resource).getLinks(rel);

        assertTrue(resource.hasLink(rel));
    }

    @Test
    public void testHasLinkByRelTwoLinksForRel() {
        String rel = UUID.randomUUID().toString();

        HyperLink link1 = makeLink(rel);
        HyperLink link2 = makeLink(rel);
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{link2, link1}).when(resource).getLinks(rel);

        assertTrue(resource.hasLink(rel));

    }

// BEGIN getLink(String profile, String name) tests
    @Test(expected = HyperResourceException.class)
    public void testGetLinkByRelAndNameZeroLinksForRel() {
        String rel = UUID.randomUUID().toString();
        String name = UUID.randomUUID().toString();
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[0]).when(resource).getLinks(rel);

        resource.getLink(rel, name);
    }

    @Test(expected = HyperResourceException.class)
    public void testGetLinkByRelAndName1LinkDifferentName() {
        String rel = UUID.randomUUID().toString();
        String name = UUID.randomUUID().toString();
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{makeLink(rel, UUID.randomUUID().toString())}).when(resource).getLinks(rel);

        resource.getLink(rel, name);
    }

    @Test
    public void testGetLinkByRelAndNameSingleLinkMatchesName() {
        String rel = UUID.randomUUID().toString();
        String name = UUID.randomUUID().toString();

        HyperLink link = makeLink(rel, name);
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{link}).when(resource).getLinks(rel);

        HyperLink result = resource.getLink(rel, name);
        assertEquals(link, result);
    }

    @Test(expected = HyperResourceException.class)
    public void testGetLinkByRelAndNameTwoLinksForRelNeitherMatch() {
        String rel = UUID.randomUUID().toString();
        String name = UUID.randomUUID().toString();

        HyperLink link1 = makeLink(rel, UUID.randomUUID().toString());
        HyperLink link2 = makeLink(rel, UUID.randomUUID().toString());
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{link2, link1}).when(resource).getLinks(rel);

        resource.getLink(rel, name);

    }

    @Test
    public void testGetLinkByRelAndNameTwoLinksForRelSecondMatches() {
        String rel = UUID.randomUUID().toString();
        String name = UUID.randomUUID().toString();

        HyperLink link1 = makeLink(rel, UUID.randomUUID().toString());
        HyperLink link2 = makeLink(rel, name);
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{link1, link2}).when(resource).getLinks(rel);


        assertEquals(link2, resource.getLink(rel, name));

    }

    @Test(expected = HyperResourceException.class)
    public void testGetLinkByRelAndNameTwoLinksForRelBothMatch() {
        String rel = UUID.randomUUID().toString();
        String name = UUID.randomUUID().toString();

        HyperLink link1 = makeLink(rel, name);
        HyperLink link2 = makeLink(rel, name);
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{link2, link1}).when(resource).getLinks(rel);

        resource.getLink(rel, name);

    }


// BEGIN hasLink(String profile, String name) tests
    @Test
    public void testHasLinkByRelAndNameZeroLinksForRel() {
        String rel = UUID.randomUUID().toString();
        String name = UUID.randomUUID().toString();
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[0]).when(resource).getLinks(rel);

        assertFalse(resource.hasLink(rel, name));
    }

    @Test
    public void testHasLinkByRelAndName1LinkDifferentName() {
        String rel = UUID.randomUUID().toString();
        String name = UUID.randomUUID().toString();
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{makeLink(rel, UUID.randomUUID().toString())}).when(resource).getLinks(rel);

        assertFalse(resource.hasLink(rel, name));
    }

    @Test
    public void testHasLinkByRelAndNameSingleLinkMatchesName() {
        String rel = UUID.randomUUID().toString();
        String name = UUID.randomUUID().toString();

        HyperLink link = makeLink(rel, name);
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{link}).when(resource).getLinks(rel);

        assertTrue(resource.hasLink(rel, name));
    }

    @Test
    public void testHasLinkByRelAndNameTwoLinksForRelNeitherMatch() {
        String rel = UUID.randomUUID().toString();
        String name = UUID.randomUUID().toString();

        HyperLink link1 = makeLink(rel, UUID.randomUUID().toString());
        HyperLink link2 = makeLink(rel, UUID.randomUUID().toString());
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{link2, link1}).when(resource).getLinks(rel);

        assertFalse(resource.hasLink(rel, name));

    }

    @Test
    public void testHasLinkByRelAndNameTwoLinksForRelSecondMatches() {
        String rel = UUID.randomUUID().toString();
        String name = UUID.randomUUID().toString();

        HyperLink link1 = makeLink(rel, UUID.randomUUID().toString());
        HyperLink link2 = makeLink(rel, name);
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{link1, link2}).when(resource).getLinks(rel);


        assertTrue(resource.hasLink(rel, name));

    }

    @Test
    public void testHasLinkByRelAndNameTwoLinksForRelBothMatch() {
        String rel = UUID.randomUUID().toString();
        String name = UUID.randomUUID().toString();

        HyperLink link1 = makeLink(rel, name);
        HyperLink link2 = makeLink(rel, name);
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{link2, link1}).when(resource).getLinks(rel);

        assertTrue(resource.hasLink(rel));

    }


// BEGIN getLinks(String profile, String name) tests
    @Test
    public void testGetLinksByRelAndNameZeroLinksForRel() {
        String rel = UUID.randomUUID().toString();
        String name = UUID.randomUUID().toString();
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[0]).when(resource).getLinks(rel);

        assertArrayEquals(new HyperLink[0], resource.getLinks(rel, name));
    }

    @Test
    public void testGetLinksByRelAndName1LinkDifferentName() {
        String rel = UUID.randomUUID().toString();
        String name = UUID.randomUUID().toString();
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{makeLink(rel, UUID.randomUUID().toString())}).when(resource).getLinks(rel);

        assertArrayEquals(new HyperLink[0], resource.getLinks(rel, name));
    }

    @Test
    public void testGetLinksByRelAndNameSingleLinkMatchesName() {
        String rel = UUID.randomUUID().toString();
        String name = UUID.randomUUID().toString();

        HyperLink link = makeLink(rel, name);
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{link}).when(resource).getLinks(rel);

        assertArrayEquals(new HyperLink[]{link}, resource.getLinks(rel, name));
    }

    @Test
    public void testGetLinksByRelAndNameTwoLinksForRelNeitherMatch() {
        String rel = UUID.randomUUID().toString();
        String name = UUID.randomUUID().toString();

        HyperLink link1 = makeLink(rel, UUID.randomUUID().toString());
        HyperLink link2 = makeLink(rel, UUID.randomUUID().toString());
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{link2, link1}).when(resource).getLinks(rel);

        assertArrayEquals(new HyperLink[0], resource.getLinks(rel, name));

    }

    @Test
    public void testGetLinksByRelAndNameTwoLinksForRelSecondMatches() {
        String rel = UUID.randomUUID().toString();
        String name = UUID.randomUUID().toString();

        HyperLink link1 = makeLink(rel, UUID.randomUUID().toString());
        HyperLink link2 = makeLink(rel, name);
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{link1, link2}).when(resource).getLinks(rel);


        assertArrayEquals(new HyperLink[]{link2}, resource.getLinks(rel, name));

    }

    @Test
    public void testGetLinksByRelAndNameTwoLinksForRelBothMatch() {
        String rel = UUID.randomUUID().toString();
        String name = UUID.randomUUID().toString();

        HyperLink link1 = makeLink(rel, name);
        HyperLink link2 = makeLink(rel, name);
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{link2, link1}).when(resource).getLinks(rel);

        assertArrayEquals(new HyperLink[]{link2, link1}, resource.getLinks(rel, name));

    }



// BEGIN getFirstMatchingLink(String profile, String...names) tests


    @Test(expected = HyperResourceException.class)
    public void testGetFirstLinkNoLinksAtAll() {
        String rel = UUID.randomUUID().toString();

        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[0]).when(resource).getLinks(rel);
        
        resource.getFirstMatchingLink(rel, "*");

    }



    @Test(expected = HyperResourceException.class)
    public void testGetFirstLinkMultipleLinksNoneMatch() {
        String rel = UUID.randomUUID().toString();

        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{makeLink(rel, UUID.randomUUID().toString()), makeLink(rel)}).when(resource).getLinks(rel);

        resource.getFirstMatchingLink(rel, "NOT A UUID");

    }

    @Test
    public void testGetFirstLinkUsingWildCard() {
        String rel = UUID.randomUUID().toString();

        HyperLink link1 = makeLink(rel, UUID.randomUUID().toString());
        HyperLink link2 = makeLink(rel, UUID.randomUUID().toString());
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{link1, link2}).when(resource).getLinks(rel);


        HyperLink result = resource.getFirstMatchingLink(rel, "*");
        assertEquals(link1, result);
    }

    @Test
    public void testGetFirstLinkUsingWildCardWithNoMatchFirst() {
        String rel = UUID.randomUUID().toString();

        HyperLink link1 = makeLink(rel, UUID.randomUUID().toString());
        HyperLink link2 = makeLink(rel, UUID.randomUUID().toString());
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{link1, link2}).when(resource).getLinks(rel);


        HyperLink result = resource.getFirstMatchingLink(rel, "NOT A UUID", "*");
        assertEquals(link1, result);
    }

    @Test
    public void testGetFirstLinkWithMatchBeforeWildcard() {
        String rel = UUID.randomUUID().toString();

        HyperLink link1 = makeLink(rel, UUID.randomUUID().toString());
        HyperLink link2 = makeLink(rel, UUID.randomUUID().toString());
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{link1, link2}).when(resource).getLinks(rel);


        HyperLink result = resource.getFirstMatchingLink(rel, link2.getName(), "*");
        assertEquals(link2, result);
    }

    @Test
    public void testGetFirstLinkWithMatchAfterNotMatching() {
        String rel = UUID.randomUUID().toString();

        HyperLink link1 = makeLink(rel, UUID.randomUUID().toString());
        HyperLink link2 = makeLink(rel, UUID.randomUUID().toString());
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{link1, link2}).when(resource).getLinks(rel);


        HyperLink result = resource.getFirstMatchingLink(rel, "not a match", "also not a match", link2.getName());
        assertEquals(link2, result);
    }

    @Test
    public void testGetFirstLinkEmptyStringName() {
        String rel = UUID.randomUUID().toString();

        HyperLink link1 = makeLink(rel, null);
        HyperLink link2 = makeLink(rel, UUID.randomUUID().toString());
        HyperLink link3 = makeLink(rel, "");
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{link1, link2, link3}).when(resource).getLinks(rel);


        HyperLink result = resource.getFirstMatchingLink(rel, "", null, link2.getName());
        assertEquals(link3, result);
    }

    @Test
    public void testGetFirstLinkNullName() {
        String rel = UUID.randomUUID().toString();

        HyperLink link1 = makeLink(rel, "");
        HyperLink link2 = makeLink(rel, UUID.randomUUID().toString());
        HyperLink link3 = makeLink(rel, null);
        HyperResource resource = mock(BaseHyperResource.class, CALLS_REAL_METHODS);
        doReturn(new HyperLink[]{link1, link2, link3}).when(resource).getLinks(rel);


        HyperLink result = resource.getFirstMatchingLink(rel, null, "", link2.getName());
        assertEquals(link3, result);
    }

}