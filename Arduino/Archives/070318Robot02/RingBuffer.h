/****************************************************************************/
/*                                                                  */
/*                                                                          */
/*    These files contain the attributes and methods needed to manage a     */
/*    ring buffer. It turns out that we do this so much, that it may be a   */
/*    good idea to just make a library that does this.                      */
/*                                                                          */
/*      BY:   Ken Wada                                                      */
/*            Aurium Technologies Inc.                                      */
/*            15-July-2013                                                  */
/*                                                                          */
/****************************************************************************/
/****************************************************************************/
/*    THE COMPANY: Aurium Technologies Inc.                                 */
/*                                                                          */
/*    THIS FIRMWARE IS FREE TO USE AND TO MODIFY. IT IS PRESENTED HERE FOR  */
/*    DEMONSTRATION PURPOSES AS AN EXAMPLE ON HOW TO CODE FOR ANY DEEPLY    */
/*    EMBEDDED SYSTEM.                                                      */
/*                                                                          */
/*    AS A RESULT, THE COMPANY SHALL NOT BE HELD LIABLE FOR ANY DIRECT,     */
/*    INDIRECT OR CONSEQUENTIAL DAMAGES WITH RESPECT TO ANY CLAIMS ARISING  */
/*    FROM THE CONTENT OF SUCH FIRMWARE AND/OR THE USE MADE BY USERS OF     */
/*    THE CODING INFORMATION CONTAINED HEREIN IN CONNECTION WITH THEIR      */
/*    PRODUCTS.                                                             */
/****************************************************************************/
/* Define to prevent recursive inclusion -------------------------------------*/
#define __RINGBUFFER_H

#define RINGBUFFER_SIZE    512

typedef struct RingBuffer
{
  uint16_t head;
  uint16_t tail;
  uint16_t count;
  uint8_t buf[RINGBUFFER_SIZE];
} RingBuffer;