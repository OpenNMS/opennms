/*
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.blast.com/
//
// Tab Size = 8
//
// iplike.c,v 1.1.1.1 2001/11/11 17:40:07 ben Exp
//
*/
#ifdef DEBUG
# include <stdio.h>		/* used for debugging */
#endif

#include <ctype.h>		/* used for isdigit() & isspace() */
#include <postgres.h>		/* PostgreSQL types */

#ifdef DEBUG
#define LOG_FILE  "/var/log/opennms/iplike.log"
#endif

/**
 * This structure is used to hold list of octets that
 * are read from the match text. I could have used dynamic
 * memory and perhaps had less of a performance hit, but
 * I decide to be brutal to the stack instead.
 *
 * Note:  This structure will be the result of 256 interget values
 * which on most platforms will be 1024 bytes of memory. There are
 * 4 of these allocated by the call to ip like, taking a full 4096
 * bytes of memory. If you feel that you are not going to have some
 * strange individual list out all the octets in a match string then
 * you might want to cut the size down.
 *
 * Also, pick an odd number of elements like 37 so that when the 
 * accesses are cached on the CPUs L1 cache the 'num' and first
 * few data members will not have a collision in the cache for
 * normal use. This may actually improve performance by perventing
 * off chip memory accesses for evaluation. I would not worry about
 * it much since there are really only four iterations and each new
 * call will change the memory. 
 *
 */
#ifndef	OCTET_LIST_MAX_SIZE
# define OCTET_LIST_MAX_SIZE 255
#endif
struct OctetList
{
	int 	num;
	int	data[OCTET_LIST_MAX_SIZE];
};
typedef struct OctetList	OctetList_t;

/**
 * The OctetRange elment is used to hold a single item from
 * an iplike match string. An octet may be either specific,
 * all, a list, or an inclusive range. MIXIING AND MATCHING
 * list, range, all, or specific IS STRICTLY FORBIDDEN BY 
 * THE MODULE.
 *
 * Thus you cannot have "129-130,254.*.*.*". That would have
 * to be split into two different rules: "129-130.*.*.*" &
 * "254.*.*.*".
 *
 */
struct OctetRange
{
#define	RANGE_TYPE_SPECIFIC  0
#define RANGE_TYPE_ALL	     1
#define RANGE_TYPE_LIST	     2
#define RANGE_TYPE_INCLUSIVE 3
	int	type;
	
	union
	{
		int		specific;
		OctetList_t	list;
		int		endpoints[2];
	} un;
};
typedef struct OctetRange	OctetRange_t;

/**
 * Converts a dotted decimal IP Address to its
 * component octets. The component octets are
 * stored in the destination address which
 * must be four in length. No bounds checking
 * is performed on the destination buffer!
 *
 * p		- Pointer to the dotted decimail address buffer
 * len		- The length of the buffer.
 * dest		- The destination buffer 
 *
 * Returns:
 *	Returns zero on success, non-zero on error!
 *
 */
static 
int convertIP(const char *p, int len, int *dest)
{
	int	octet = 0;	/* used for conveting the text to binary data */
	int	ndx   = 0;	/* index into the dest buffer */
	int	hadDigit = 0;	/* set to one when a digit is encountered */
	
	/*
	 * check to make sure that the data is value
	 */
	if(dest == NULL || p == NULL || len <= 0)
		return -1;
	
	/*
	 * shift past the non character data
	 * and subtract for the invalid data on the end
	 */
	while(len > 0 && isspace(*p))
		--len, p++;
	
	while(len > 0 && (isspace(p[len-1]) || p[len-1] == '\0'))
		--len;
	
	/*
	 * check the length again
	 */
	if(len <= 0)
		return -1;
	
	/*
	 * init the array
	 */
	for(ndx = 0; ndx < 4; ndx++)
		dest[ndx] = -1;
	
	/*
	 * decode the octets
	 */
	ndx = 0;
	while(len > 0 && ndx < 4)
	{
		if(isdigit(*p))
		{	
			/*
			 * convert the digit and multiply the 
			 * current value by 10. Remember it's 
			 * dotted decimal (that's base 10!)
			 */
			octet = (octet * 10) + ((*p) - '0');
			hadDigit = 1;
		}
		else if((*p) == '.')
		{
			/**
			 * Store off the octet, but perform
			 * some basic checks
			 */
			if(octet > 255 || hadDigit == 0)
				return -1;
			
			hadDigit     = 0;
			dest[ndx++]  = octet;
			octet        = 0;
		}
		else
		{
			/**
			 * invalid character
			 */
			return -1;
		}
		
		/*
		 * next please
		 */
		++p;
		--len;
	}
	
	/*
	 * handle the case were we ran out of buffer
	 */
	if(ndx == 3 && hadDigit != 0)
	{
		/**
		 * Again just perform some basic checks
		 */
		if(octet > 255)
			return -1;
		
		dest[ndx++] = octet;
	}
	
	/**
	 * if we got four octets then it was
	 * successful, else it was not and an
	 * error condition needs to be returned.
	 */
	return (ndx == 4 ? 0 : -1);
}

/**
 * Converts a dotted decimal IP Like Address to its
 * component octets. The component octets are
 * stored in the destination address which
 * must be four in length. No bounds checking
 * is performed on the destination buffer!
 *
 * p		- Pointer to the dotted decimail address buffer
 * len		- The length of the buffer.
 * dest		- The destination buffer 
 *
 * Returns:
 *	Returns zero on success, non-zero on error!
 *
 */
static
int getRangeInfo(const char *p, int len, OctetRange_t *dest)
{
	int	ndx      = 0;	/* index into the dest parameter */
	int	hadDigit = 0;	/* set true when a digit is encountered */
	int	isAll	 = 0;	/* set true when a '*' is encountered */
	int	isRange  = 0;	/* range: x-y */
	int	isList	 = 0;	/* list: x,y,z */
	int	octet	 = 0;	/* the octet value, if any */
	int	i;
	
	/**
	 * perform some basic checks
	 */
	if(p == NULL || len <= 0 || dest == NULL)
		return -1;
		
	/*
 	 * increment past the space chars
	 */
	while(isspace(*p) && len > 0)
		++p, --len;
	
	/*
	 * shift space off the end
	 */
	while(len > 0 && (isspace(p[len-1]) || p[len-1] == '\0'))
		--len;
	
	/**
	 * Another basic check
	 */
	if(len <= 0)
		return -1;
	
	/*
	 * count the number of dots, must equal 3
	 */
	for(i = 0; i < len && ndx < 3; i++)
	{
		if(*(p+i) == '.')
			++ndx;
	}
	if(ndx != 3)
		return -1;
	
	/*
	 * it's ok so decode it
	 */
	ndx   = 0;
	octet = 0;
	while(len > 0 && ndx < 4)
	{
		if(*p == '*')	/* all inclusive */
		{
			/**
			 * sanity check
			 */
			if(hadDigit || isRange || isList || (len > 1 &&  *(p+1) != '.'))
				return -1;
			
			isAll = 1;
		}
		else if(*p == ',')
		{
			/**
			 * sanity check 
			 */
			if(!hadDigit || isRange || isAll || octet > 255)
				return -1;
			
			if(!isList)
			{
				isList = 1;
				dest[ndx].un.list.num = 1;
				dest[ndx].un.list.data[0] = octet;
			}
			else
			{
				dest[ndx].un.list.data[dest[ndx].un.list.num++] = octet;
			}
			hadDigit = 0;
			octet    = 0;
		}
		else if(*p == '-')
		{
			/**
			 * sanity check
			 */
			if(!hadDigit || isList || isAll || octet > 255)
				return -1;
			
			if(!isRange)
			{
				isRange = 1;
				dest[ndx].un.endpoints[0] = octet;
			}
			else
			{
				/*
				 * can only have isRange set once!
				 */
				return -1;
			}
				
			hadDigit = 0;
			octet    = 0;
		}
		else if(isdigit(*p))
		{
			octet = (octet * 10) + ((*p) - '0');
			hadDigit = 1;
		}
		else if((*p) == '.')
		{
			/**
			 * basic santity check
			 */
			if(octet > 255 || (hadDigit == 0 && !isAll) || (isList && isRange))
				return -1;
			
			/**
			 * what type of value is it?
			 */
			if(isRange)
			{
				dest[ndx].type = RANGE_TYPE_INCLUSIVE;
				dest[ndx].un.endpoints[1] = octet;
				if(dest[ndx].un.endpoints[0] > dest[ndx].un.endpoints[1])
				{
					/*
					 * swap
					 */
					int swap = dest[ndx].un.endpoints[0];
					dest[ndx].un.endpoints[0] = dest[ndx].un.endpoints[1];
					dest[ndx].un.endpoints[1] = swap;
				}
				ndx++;
				isRange = 0;
			}
			else if(isList)
			{
				dest[ndx].type = RANGE_TYPE_LIST;
				dest[ndx].un.list.data[dest[ndx].un.list.num++] = octet;
				ndx++;
				isList = 0;
			}
			else if(isAll)
			{
				dest[ndx++].type = RANGE_TYPE_ALL;
				isAll = 0;
			}
			else
			{
				dest[ndx].type = RANGE_TYPE_SPECIFIC;
				dest[ndx].un.specific = octet;
				ndx++;
			}
			hadDigit     = 0;
			octet        = 0;
		}
		else
		{
			return -1;
		}
		
		/*
		 * next please
		 */
		++p;
		--len;
	}
	
	/*
	 * handle the case were we ran out of buffer
	 */
	if(ndx == 3)
	{
		if(octet > 255 || (hadDigit == 0 && !isAll) || (isList && isRange))
			return -1;

		if(isRange)
		{
			dest[ndx].type = RANGE_TYPE_INCLUSIVE;
			dest[ndx].un.endpoints[1] = octet;
			ndx++;
			isRange = 0;
		}
		else if(isList)
		{
			dest[ndx].type = RANGE_TYPE_LIST;
			dest[ndx].un.list.data[dest[ndx].un.list.num++] = octet;
			ndx++;
			isList = 0;
		}
		else if(isAll)
		{
			dest[ndx++].type = RANGE_TYPE_ALL;
			isAll = 0;
		}
		else
		{
			dest[ndx].type = RANGE_TYPE_SPECIFIC;
			dest[ndx].un.specific = octet;
			ndx++;
		}
	}
	
	/**
	 * return the result
	 */
	return (ndx == 4 ? 0 : -1);
}

/**
 * Compares the IP Address against the match string
 * and returns true if the IP Address is part of
 * the passed range.
 *
 *
 * Returns:
 *	Returns true if it matches, false if it does not
 *
 */
bool iplike(text *value, text *rule)
{
	bool		rcode = false;		/* the return code */
	int		i,j;			/* loop variables */
	int		octets[4];		/* the split apart ip address */
	OctetRange_t	ranges[4];		/* the convert match ranges */

	/**
	 * basic santiy check
	 */
	if(value == NULL || rule == NULL)
		return false;

	/*
	 * Decode the address
	 */
	rcode = (convertIP((const char *)VARDATA(value),
			   VARSIZE(value)-VARHDRSZ, 
			   octets) == 0 
		 ? true : false);
	if(rcode == false)
	{
		return false;
	}
	
	/*
	 * decode the next parameters
	 */
	rcode = (getRangeInfo((const char *)VARDATA(rule), 
			      VARSIZE(rule)-VARHDRSZ, 
			      ranges) == 0 
		 ? true : false); 
	if(rcode == false)
	{
		return false;
	}
		
	/*
	 * now do the comparisions
	 */
	rcode = true;
	for(i = 0; i < 4 && rcode != false; i++)
	{
		switch(ranges[i].type)
		{
		case RANGE_TYPE_SPECIFIC:
			rcode = (octets[i] == ranges[i].un.specific ? true : false);
			break;
			
		case RANGE_TYPE_LIST:
			rcode = false;
			for(j = ranges[i].un.list.num-1; j >= 0 && rcode == false; --j)
			{
				if(octets[i] == ranges[i].un.list.data[j])
					rcode = true;
			}
			break;
			
		case RANGE_TYPE_INCLUSIVE:
			if(octets[i] < ranges[i].un.endpoints[0] ||
			   octets[i] > ranges[i].un.endpoints[1])
				rcode = false;
			break;
			
		case RANGE_TYPE_ALL:
			rcode = true;
			break;
		
		default:
			rcode = false;
			break;
		}
	}
	
	return rcode;
}

#ifdef DEBUG
int main(int argc, char **argv)
{
	text *	arg1;
	text *	arg2;
	char	arg1_buf[1024];
	char	arg2_buf[1024];
	
	if(argc != 3)
		return 1;
	
	
	arg1 = (text *)arg1_buf;
	arg2 = (text *)arg2_buf;
#ifdef TUPLE_TOASTER_ACTIVE
	VARATT_SIZEP(arg1) = strlen(argv[1])+VARHDRSZ;
	VARATT_SIZEP(arg2) = strlen(argv[2])+VARHDRSZ;
#else
	VARSIZE(arg1) = strlen(argv[1])+VARHDRSZ;
	VARSIZE(arg2) = strlen(argv[2])+VARHDRSZ;
#endif
	strcpy(VARDATA(arg1), argv[1]);
	strcpy(VARDATA(arg2), argv[2]);
	
	return (iplike(arg1, arg2) == true ? 0 : 1);
}
#endif

