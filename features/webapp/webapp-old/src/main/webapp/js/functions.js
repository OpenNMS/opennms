//verifies that a var is an integer
function isInteger(number)
{
        for (i=0; i < number.length; i++)
        {
                if (isNaN(number.charAt(i)))
                {
                        return false;
                }
        }
        
        return true;
}

//ensures that the contents of four octet boxes are integers within normal octet bounds (0-255)
//returns true if all are ok, false otherwise and pops up a message box
function verifyAddress(octet1, octet2, octet3, octet4, field)
{
        if (!isInteger(octet1) || octet1 == "" || octet1 < 0 || octet1 > 255)
        {
                alert("The first octet of the " + field + " must be a number between 0 and 255.");
                return false;
        }
        if (!isInteger(octet2) || octet2 == "" || octet2 < 0 || octet2 > 255)
        {
                alert("The second octet of the " + field + " must be a number between 0 and 255.");
                return false;
        }
        if (!isInteger(octet3) || octet3 == "" || octet3 < 0 || octet3 > 255)
        {
                alert("The third octet of the " + field + " must be a number between 0 and 255.");
                return false;
        }
        if (!isInteger(octet4) || octet4 == "" || octet4 < 0 || octet4 > 255)
        {
                alert("The fourth octet of the " + field + " must be a number between 0 and 255.");
                return false;
        }
        return true;
}
