/*
 * Copyright (c) 2005 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright (c) 2010 JogAmp Community. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 * 
 * Sun gratefully acknowledges that this software was originally authored
 * and developed by Kenneth Bradley Russell and Christopher John Kline.
 */

package com.jogamp.gluegen.cgram.types;

import com.jogamp.common.os.MachineDescription;

/** Provides a level of indirection between the definition of a type's
    size and the absolute value of this size. Necessary when
    generating glue code for two different CPU architectures (e.g.,
    32-bit and 64-bit) from the same internal representation of the
    various types involved. */

public abstract class SizeThunk implements Cloneable {
  // Private constructor because there are only a few of these
  private SizeThunk() {}

  public Object clone() {
    try {
        return super.clone();
    } catch (CloneNotSupportedException ex) {
        throw new InternalError();
    }
  }

  public abstract long computeSize(MachineDescription machDesc);
  public abstract long computeAlignment(MachineDescription machDesc);

  public static final SizeThunk INT8 = new SizeThunk() {
      public long computeSize(MachineDescription machDesc) {
        return machDesc.int8SizeInBytes();
      }
      public long computeAlignment(MachineDescription machDesc) {
        return machDesc.int8AlignmentInBytes();
      }
    };

  public static final SizeThunk INT16 = new SizeThunk() {
      public long computeSize(MachineDescription machDesc) {
        return machDesc.int16SizeInBytes();
      }
      public long computeAlignment(MachineDescription machDesc) {
        return machDesc.int16AlignmentInBytes();
      }
    };

  public static final SizeThunk INT32 = new SizeThunk() {
      public long computeSize(MachineDescription machDesc) {
        return machDesc.int32SizeInBytes();
      }
      public long computeAlignment(MachineDescription machDesc) {
        return machDesc.int32AlignmentInBytes();
      }
    };

  public static final SizeThunk INTxx = new SizeThunk() {
      public long computeSize(MachineDescription machDesc) {
        return machDesc.intSizeInBytes();
      }
      public long computeAlignment(MachineDescription machDesc) {
        return machDesc.intAlignmentInBytes();
      }
    };

  public static final SizeThunk LONG = new SizeThunk() {
      public long computeSize(MachineDescription machDesc) {
        return machDesc.longSizeInBytes();
      }
      public long computeAlignment(MachineDescription machDesc) {
        return machDesc.longAlignmentInBytes();
      }
    };

  public static final SizeThunk INT64 = new SizeThunk() {
      public long computeSize(MachineDescription machDesc) {
        return machDesc.int64SizeInBytes();
      }
      public long computeAlignment(MachineDescription machDesc) {
        return machDesc.int64AlignmentInBytes();
      }
    };

  public static final SizeThunk FLOAT = new SizeThunk() {
      public long computeSize(MachineDescription machDesc) {
        return machDesc.floatSizeInBytes();
      }
      public long computeAlignment(MachineDescription machDesc) {
        return machDesc.floatAlignmentInBytes();
      }
    };

  public static final SizeThunk DOUBLE = new SizeThunk() {
      public long computeSize(MachineDescription machDesc) {
        return machDesc.doubleSizeInBytes();
      }
      public long computeAlignment(MachineDescription machDesc) {
        return machDesc.doubleAlignmentInBytes();
      }
    };

  public static final SizeThunk POINTER = new SizeThunk() {
      public long computeSize(MachineDescription machDesc) {
        return machDesc.pointerSizeInBytes();
      }
      public long computeAlignment(MachineDescription machDesc) {
        return machDesc.pointerAlignmentInBytes();
      }
    };

  // Factory methods for performing certain limited kinds of
  // arithmetic on these values
  public static SizeThunk add(final SizeThunk thunk1,
                              final SizeThunk thunk2) {
    return new SizeThunk() {
        public long computeSize(MachineDescription machDesc) {
          return thunk1.computeSize(machDesc) + thunk2.computeSize(machDesc);
        }
        public long computeAlignment(MachineDescription machDesc) {
          final long thunk1A = thunk1.computeAlignment(machDesc); 
          final long thunk2A = thunk2.computeAlignment(machDesc);
          return ( thunk1A > thunk2A ) ? thunk1A : thunk2A ;
        }
      };
  }

  public static SizeThunk sub(final SizeThunk thunk1,
                              final SizeThunk thunk2) {
    return new SizeThunk() {
        public long computeSize(MachineDescription machDesc) {
          return thunk1.computeSize(machDesc) - thunk2.computeSize(machDesc);
        }
        public long computeAlignment(MachineDescription machDesc) {
          // FIXME
          final long thunk1A = thunk1.computeAlignment(machDesc); 
          final long thunk2A = thunk2.computeAlignment(machDesc);
          return ( thunk1A > thunk2A ) ? thunk1A : thunk2A ;
        }
      };
  }

  public static SizeThunk mul(final SizeThunk thunk1,
                              final SizeThunk thunk2) {
    return new SizeThunk() {
        public long computeSize(MachineDescription machDesc) {
          return thunk1.computeSize(machDesc) * thunk2.computeSize(machDesc);
        }
        public long computeAlignment(MachineDescription machDesc) {
          final long thunk1A = thunk1.computeAlignment(machDesc); 
          final long thunk2A = thunk2.computeAlignment(machDesc);
          return ( thunk1A > thunk2A ) ? thunk1A : thunk2A ;
        }
      };
  }

  public static SizeThunk mod(final SizeThunk thunk1,
                              final SizeThunk thunk2) {
    return new SizeThunk() {
        public long computeSize(MachineDescription machDesc) {
          return thunk1.computeSize(machDesc) % thunk2.computeSize(machDesc);
        }
        public long computeAlignment(MachineDescription machDesc) {
          // FIXME
          final long thunk1A = thunk1.computeAlignment(machDesc); 
          final long thunk2A = thunk2.computeAlignment(machDesc);
          return ( thunk1A > thunk2A ) ? thunk1A : thunk2A ;
        }
      };
  }

  public static SizeThunk roundUp(final SizeThunk thunk1,
                                  final SizeThunk thunk2) {
    return new SizeThunk() {
        public long computeSize(MachineDescription machDesc) {
          final long sz1 = thunk1.computeSize(machDesc);
          final long sz2 = thunk2.computeSize(machDesc);
          final long rem = (sz1 % sz2);
          if (rem == 0) {
            return sz1;
          }
          return sz1 + (sz2 - rem);
        }
        public long computeAlignment(MachineDescription machDesc) {
          final long thunk1A = thunk1.computeAlignment(machDesc); 
          final long thunk2A = thunk2.computeAlignment(machDesc);
          return ( thunk1A > thunk2A ) ? thunk1A : thunk2A ;
        }
      };
  }

  public static SizeThunk max(final SizeThunk thunk1,
                              final SizeThunk thunk2) {
    return new SizeThunk() {
        public long computeSize(MachineDescription machDesc) {
          return Math.max(thunk1.computeSize(machDesc), thunk2.computeSize(machDesc));
        }
        public long computeAlignment(MachineDescription machDesc) {
          final long thunk1A = thunk1.computeAlignment(machDesc); 
          final long thunk2A = thunk2.computeAlignment(machDesc);
          return ( thunk1A > thunk2A ) ? thunk1A : thunk2A ;
        }
      };
  }

  public static SizeThunk constant(final int constant) {
    return new SizeThunk() {
        public long computeSize(MachineDescription machDesc) {
          return constant;
        }
        public long computeAlignment(MachineDescription machDesc) {
          return 1; // no real alignment for constants 
        }        
      };
  }
}
