/*

   Derby - Class com.pivotal.gemfirexd.internal.iapi.services.io.RegisteredFormatIds

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package com.pivotal.gemfirexd.internal.iapi.services.io;

import com.pivotal.gemfirexd.internal.iapi.services.info.JVMInfo;
import com.pivotal.gemfirexd.internal.iapi.services.sanity.SanityManager;

/**
        Registration of TypedFormat classes.

        <P>
        A TypedFormat is registered by placing a class name at the
        correct place in the correct array, driven by the base format number:
        <UL>
        <LI>2 byte - MIN_TWO_BYTE_FORMAT_ID - TwoByte
        </UL>
        The offset from the base format number (0 based) gives the offset in the array.
        <P>
        The class name is either:
        <UL>
        <LI> The actual class name of the TypeFormat.
        <LI> The name of a class that extends com.pivotal.gemfirexd.internal.iapi.services.io.FormatableInstanceGetter.
             In this case the monitor will register an instance of the class after calling its
                 setFormatId() method with format id it is registered as.
        </UL>
*/

public interface RegisteredFormatIds {

/* one byte  format identifiers never used
String[] OneByte = {
};
*/

String[] TwoByte = {
        /* 0 */         null, // null marker
        /* 1 */         null, // String marker
        /* 2 */         null, // Serializable marker
        /* 3 */         null,
        /* 4 */         null,
        /* 5 */         null,
        /* 6 */         null,
        /* 7 */         null,
        /* 8 */         null,
        /* 9 */         null,
        /* 10 */        null,
        /* 11 */        null,
        /* 12 */        null,
        /* 13 */        null,
        /* 14 */        "com.pivotal.gemfirexd.internal.catalog.types.TypeDescriptorImpl",
        /* 15 */        "com.pivotal.gemfirexd.internal.impl.store.access.PC_XenaVersion",
        /* 16 */        "com.pivotal.gemfirexd.internal.catalog.types.TypesImplInstanceGetter",
        /* 17 */        "com.pivotal.gemfirexd.internal.catalog.types.TypesImplInstanceGetter",
        /* 18 */        "com.pivotal.gemfirexd.internal.catalog.types.TypesImplInstanceGetter",
        /* 19 */        "com.pivotal.gemfirexd.internal.catalog.types.TypesImplInstanceGetter",
        /* 20 */        "com.pivotal.gemfirexd.internal.catalog.types.TypesImplInstanceGetter",
        /* 21 */        "com.pivotal.gemfirexd.internal.catalog.types.TypesImplInstanceGetter",
        /* 22 */        "com.pivotal.gemfirexd.internal.catalog.types.TypesImplInstanceGetter",
        /* 23 */        "com.pivotal.gemfirexd.internal.catalog.types.TypesImplInstanceGetter",
        /* 24 */        null,
        /* 25 */        "com.pivotal.gemfirexd.internal.catalog.types.TypesImplInstanceGetter",
        /* 26 */        null,
        /* 27 */        null,
        /* 28 */        "com.pivotal.gemfirexd.internal.catalog.types.TypesImplInstanceGetter",
        /* 29 */        null,
        /* 30 */        "com.pivotal.gemfirexd.internal.catalog.types.TypesImplInstanceGetter",
        /* 31 */        "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 32 */        "com.pivotal.gemfirexd.internal.catalog.types.TypesImplInstanceGetter",
        /* 33 */        "com.pivotal.gemfirexd.internal.catalog.types.TypesImplInstanceGetter",
        /* 34 */        "com.pivotal.gemfirexd.internal.catalog.types.TypesImplInstanceGetter",
        /* 35 */        null,
        /* 36 */        null,
        /* 37 */        "com.pivotal.gemfirexd.internal.impl.sql.execute.DeleteConstantAction",
        /* 38 */        "com.pivotal.gemfirexd.internal.impl.sql.execute.InsertConstantAction",
        /* 39 */        "com.pivotal.gemfirexd.internal.impl.sql.execute.UpdateConstantAction",
        /* 40 */        null,
        /* 41 */        null,
        /* 42 */        null,
        /* 43 */        null,
        /* 44 */        null,
        /* 45 */        null,
        /* 46 */        null,
        /* 47 */        null,
        /* 48 */        null,
        /* 49 */        null,
        /* 50 */        null,
        /* 51 */        null,
        /* 52 */        null,
        /* 53 */        null,
        /* 54 */        null,
        /* 55 */        null,
        /* 56 */        null,
        /* 57 */        null,
        /* 58 */        null,
        /* 59 */        null,
        /* 60 */        null,
        /* 61 */        null,
        /* 62 */        null,
        /* 63 */        null,
        /* 64 */        null,
        /* 65 */        null,
        /* 66 */        null,
        /* 67 */        null,
        /* 68 */        null,
        /* 69 */        null,
        /* 70 */        null,
        /* 71 */        null,
        /* 72 */        null,
        /* 73 */        null, 
        /* 74 */        null,
        /* 75 */        null,
        /* 76 */        null,
        /* 77 */        "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 78 */        "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 79 */        "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 80 */        "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 81 */        "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 82 */        "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 83 */        "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 84 */        "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 85 */        "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 86 */        null,
        /* 87 */        "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 88 */        "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 89 */        "com.pivotal.gemfirexd.internal.iapi.types.SQLLongint",
        /* 90 */        "com.pivotal.gemfirexd.internal.impl.store.access.heap.HeapClassInfo",
        /* 91 */        "com.pivotal.gemfirexd.internal.impl.store.access.heap.Heap_v10_2",
        /* 92 */        null,
        /* 93 */        "com.pivotal.gemfirexd.internal.impl.store.access.StorableFormatId",
        /* 94 */        null,
        /* 95 */        "com.pivotal.gemfirexd.internal.impl.store.access.btree.index.B2IUndo",
        /* 96 */        null,
        /* 97 */        "com.pivotal.gemfirexd.internal.impl.store.raw.data.ChainAllocPageOperation",
        /* 98 */        null,
        /* 99 */        null,
        /* 100 */       null,
        /* 101 */       "com.pivotal.gemfirexd.internal.impl.store.raw.data.DeleteOperation",
        /* 102 */       "com.pivotal.gemfirexd.internal.impl.store.raw.xact.EndXact",
        /* 103 */       "com.pivotal.gemfirexd.internal.impl.store.raw.data.InsertOperation",
        /* 104 */       "com.pivotal.gemfirexd.internal.impl.store.raw.data.LogicalUndoOperation",
        /* 105 */       "com.pivotal.gemfirexd.internal.impl.store.raw.data.PhysicalUndoOperation",
        /* 106 */       "com.pivotal.gemfirexd.internal.impl.store.raw.data.PurgeOperation",
        /* 107 */       "com.pivotal.gemfirexd.internal.impl.store.raw.data.ContainerUndoOperation",
        /* 108 */       "com.pivotal.gemfirexd.internal.impl.store.raw.data.UpdateOperation",
        /* 109 */       "com.pivotal.gemfirexd.internal.impl.store.raw.data.UpdateFieldOperation",
        /* 110 */       null,
        /* 111 */       "com.pivotal.gemfirexd.internal.impl.store.raw.data.AllocPageOperation",
        /* 112 */       null,
        /* 113 */       "com.pivotal.gemfirexd.internal.impl.store.raw.data.InvalidatePageOperation",
        /* 114 */       "com.pivotal.gemfirexd.internal.impl.store.raw.log.SaveLWMOperation",
        /* 115 */       null, 
        /* 116 */       null,
        /* 117 */       "com.pivotal.gemfirexd.internal.impl.store.raw.data.StoredPage",
        /* 118 */       "com.pivotal.gemfirexd.internal.impl.store.raw.data.AllocPage",
        /* 119 */       null,
        /* 120 */       null,
        /* 121 */       null,
        /* 122 */       null,
        /* 123 */       null,
        /* 124 */       null,
        /* 125 */       null,
        /* 126 */       null,
        /* 127 */       null,
        /* 128 */       null,
        /* 129 */       "com.pivotal.gemfirexd.internal.impl.store.raw.log.LogRecord",
        /* 130 */       "com.pivotal.gemfirexd.internal.impl.store.raw.log.LogCounter",
        /* 131 */       "com.pivotal.gemfirexd.internal.impl.services.uuid.BasicUUIDGetter",           // InstanceGetter
        /* 132 */       null,
        /* 133 */       "com.pivotal.gemfirexd.internal.impl.store.access.btree.LeafControlRow",
        /* 134 */       "com.pivotal.gemfirexd.internal.impl.store.access.btree.BranchControlRow",
        /* 135 */       "com.pivotal.gemfirexd.internal.impl.sql.catalog.CoreDDFinderClassInfo",             // InstanceGetter
        /* 136 */       "com.pivotal.gemfirexd.internal.impl.sql.catalog.CoreDDFinderClassInfo",             // InstanceGetter
        /* 137 */       "com.pivotal.gemfirexd.internal.impl.sql.catalog.CoreDDFinderClassInfo",             // InstanceGetter
        /* 138 */       null,
        /* 139 */       null,
        /* 140 */       null,
        /* 141 */       null,
        /* 142 */       null,
        /* 143 */       null,
        /* 144 */       null,
        /* 145 */       "com.pivotal.gemfirexd.internal.impl.sql.catalog.CoreDDFinderClassInfo",             // InstanceGetter
        /* 146 */       null,
        /* 147 */       "com.pivotal.gemfirexd.internal.impl.store.raw.xact.XactId",
        /* 148 */       null,
        /* 149 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.AvgAggregator",
        /* 150 */       null,
        /* 151 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.CountAggregator",
        /* 152 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.MaxMinAggregator",
        /* 153 */       null,
        /* 154 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.SumAggregator",
        /* 155 */       null,
        /* 156 */       null, 
        /* 157 */       null, 
        /* 158 */       null, 
        /* 159 */       null, 
        /* 160 */       null, 
        /* 161 */       null, 
        /* 162 */       null, 
        /* 163 */       null, 
        /* 164 */       null, 
        /* 165 */       null, 
        /* 166 */       null, 
        /* 167 */       null, 
        /* 168 */       null, 
        /* 169 */       "com.pivotal.gemfirexd.internal.impl.store.raw.xact.BeginXact",
        /* 170 */       null, 
        /* 171 */       null, 
        /* 172 */       null,
        /* 173 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RunTimeStatisticsImpl",
        /* 174 */       null,
        /* 175 */       null,
        /* 176 */       null,
        /* 177 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealProjectRestrictStatistics",
        /* 178 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealSortStatistics",
        /* 179 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealTableScanStatistics",
        /* 180 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealNestedLoopJoinStatistics",
        /* 181 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealIndexRowToBaseRowStatistics",
        /* 182 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealAnyResultSetStatistics",
        /* 183 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealOnceResultSetStatistics",
        /* 184 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealCurrentOfStatistics",
        /* 185 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealRowResultSetStatistics",
        /* 186 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealUnionResultSetStatistics",
        /* 187 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealNestedLoopLeftOuterJoinStatistics",
        /* 188 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealNormalizeResultSetStatistics",
        /* 189 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealInsertResultSetStatistics",
        /* 190 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealUpdateResultSetStatistics",
        /* 191 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealDeleteResultSetStatistics",
        /* 192 */       null,
        /* 193 */       null,
        /* 194 */       null,
        /* 195 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 196 */       "com.pivotal.gemfirexd.internal.catalog.types.TypesImplInstanceGetter",
        /* 197 */       null,
        /* 198 */       "com.pivotal.gemfirexd.internal.catalog.types.TypesImplInstanceGetter",
        /* 199 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter,
        /* 200 */       null, // DECIMAL - register dynamically by DataValueFactory implementation
        /* 201 */       null,
        /* 202 */       "com.pivotal.gemfirexd.internal.iapi.types.UserType",
        /* 203 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealHashScanStatistics",
        /* 204 */       null,
        /* 205 */       "com.pivotal.gemfirexd.internal.catalog.types.ReferencedColumnsDescriptorImpl",
        /* 206 */       null,
        /* 207 */       null,
        /* 208 */       "com.pivotal.gemfirexd.internal.impl.sql.catalog.CoreDDFinderClassInfo",             // InstanceGetter
        /* 209 */       null,
        /* 210 */       "com.pivotal.gemfirexd.internal.impl.store.raw.data.CopyRowsOperation",
        /* 211 */       null,
        /* 212 */       null,
        /* 213 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.ReplaceJarConstantAction",
        /* 214 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealVTIStatistics",
        /* 215 */       null,
        /* 216 */       null,
        /* 217 */       null,
        /* 218 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.IndexColumnOrder",
        /* 219 */       "com.pivotal.gemfirexd.internal.iapi.util.ByteArray",
        /* 220 */       null,
        /* 221 */       null,
        /* 222 */       null,
        /* 223 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.AggregatorInfo",
        /* 224 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.AggregatorInfoList",
        /* 225 */       "com.pivotal.gemfirexd.internal.impl.sql.GenericStorablePreparedStatement",
        /* 226 */       "com.pivotal.gemfirexd.internal.impl.sql.catalog.CoreDDFinderClassInfo",             // InstanceGetter
        /* 227 */       null,
        /* 228 */       "com.pivotal.gemfirexd.internal.impl.sql.GenericResultDescription",
        /* 229 */       null,
        /* 230 */       null,
        /* 231 */       "com.pivotal.gemfirexd.internal.catalog.types.TypesImplInstanceGetter",
        /* 232 */       null,
        /* 233 */       "com.pivotal.gemfirexd.internal.catalog.types.TypesImplInstanceGetter",
        /* 234 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter,
        /* 235 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter,
        /* 236 */       null,
        /* 237 */       null,
        /* 238 */       null,
        /* 239 */       null,
        /* 240 */       "com.pivotal.gemfirexd.internal.iapi.types.DataTypeDescriptor",
        /* 241 */       "com.pivotal.gemfirexd.internal.impl.store.raw.data.InitPageOperation",
        /* 242 */       "com.pivotal.gemfirexd.internal.impl.store.raw.data.ContainerOperation",
        /* 243 */       null,
        /* 244 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 245 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 246 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 247 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 248 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 249 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 250 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 251 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 252 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 253 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 254 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 255 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 256 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 257 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 258 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 259 */       "com.pivotal.gemfirexd.internal.catalog.types.TypesImplInstanceGetter", // old catalog type format
        /* 260 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 261 */       "com.pivotal.gemfirexd.internal.impl.store.raw.xact.TransactionTableEntry",
        /* 262 */       "com.pivotal.gemfirexd.internal.impl.store.raw.xact.TransactionTable",
        /* 263 */       "com.pivotal.gemfirexd.internal.impl.store.raw.log.CheckpointOperation",
        /* 264 */       "com.pivotal.gemfirexd.internal.catalog.types.UserDefinedTypeIdImpl",
        /* 265 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 266 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 267 */       null,
        /* 268 */       "com.pivotal.gemfirexd.internal.iapi.sql.dictionary.IndexRowGenerator",
        /* 269 */       "com.pivotal.gemfirexd.internal.iapi.services.io.FormatableBitSet",
        /* 270 */       "com.pivotal.gemfirexd.internal.iapi.services.io.FormatableArrayHolder",
        /* 271 */       "com.pivotal.gemfirexd.internal.iapi.services.io.FormatableProperties",
        /* 272 */       null,
        /* 273 */       "com.pivotal.gemfirexd.internal.impl.sql.catalog.CoreDDFinderClassInfo",             // InstanceGetter
        /* 274 */       null,
        /* 275 */       null,
        /* 276 */       null,
        /* 277 */       null,
        /* 278 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.ConstraintInfo",
        /* 279 */       null,
        /* 280 */       null,
        /* 281 */       null,
        /* 282 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.FKInfo",
        /* 283 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealScalarAggregateStatistics",
        /* 284 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealDistinctScalarAggregateStatistics",
        /* 285 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealGroupedAggregateStatistics",
        /* 286 */       null,
        /* 287 */       "com.pivotal.gemfirexd.internal.impl.store.raw.data.SetReservedSpaceOperation",
        /* 288 */    null,
        /* 289 */       null,
        /* 290 */       null,
        /* 291 */       "com.pivotal.gemfirexd.internal.impl.store.raw.data.RemoveFileOperation",
        /* 292 */       null,
        /* 293 */       null,
        /* 294 */       null,
        /* 295 */       null,
        /* 296 */       "com.pivotal.gemfirexd.internal.impl.sql.CursorTableReference",
        /* 297 */       "com.pivotal.gemfirexd.internal.impl.sql.CursorInfo",
        /* 298 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 299 */       "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 300 */       null,
        /* 301 */       null,
        /* 302 */       null,
        /* 303 */       "com.pivotal.gemfirexd.internal.iapi.services.io.FormatableIntHolder",
        /* 304 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealHashJoinStatistics",
        /* 305 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealHashLeftOuterJoinStatistics",
        /* 306 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealHashTableStatistics",
        /* 307 */       "com.pivotal.gemfirexd.internal.iapi.types.JSQLType",
        /* 308 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealMaterializedResultSetStatistics",
        /* 309 */       null,
        /* 310 */       null,
        /* 311 */       null,
        /* 312 */       "com.pivotal.gemfirexd.internal.catalog.types.MethodAliasInfo",
        /* 313 */       "com.pivotal.gemfirexd.internal.iapi.services.io.FormatableHashtable",
        /* 314 */       null,
        /* 315 */       null,
        /* 316 */       "com.pivotal.gemfirexd.internal.iapi.sql.dictionary.TriggerDescriptor",
        /* 317 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.TriggerInfo",
        /* 318 */       null,
        /* 319 */       null,
        /* 320 */       "com.pivotal.gemfirexd.internal.impl.sql.catalog.CoreDDFinderClassInfo",             // InstanceGetter
        /* 321 */       null,
        /* 322 */       null,
        /* 323 */       null,
        /* 324 */       null,
        /* 325 */       "com.pivotal.gemfirexd.internal.impl.sql.catalog.CoreDDFinderClassInfo",             // InstanceGetter
        /* 326 */       "com.pivotal.gemfirexd.internal.catalog.types.DefaultInfoImpl",
        /* 327 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealLastIndexKeyScanStatistics",
        /* 328 */       "com.pivotal.gemfirexd.internal.impl.store.raw.xact.GlobalXactId",
        /* 329 */       "com.pivotal.gemfirexd.internal.iapi.services.io.FormatableLongHolder",
        /* 330 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealScrollInsensitiveResultSetStatistics",
        /* 331 */       null,
        /* 332 */       null,
        /* 333 */       null,
        /* 334 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealDistinctScanStatistics",
        /* 335 */       null,
        /* 336 */       null,
        /* 337 */       null,
        /* 338 */       null,
        /* 339 */       null,
        /* 340 */       null,
        /* 341 */       null,
        /* 342 */       null,
        /* 343 */       null,
        /* 344 */       null,
        /* 345 */       null,
        /* 346 */       null,
        /* 347 */       null,
        /* 348 */       null,
        /* 349 */       null,
        /* 350 */       null,
        /* 351 */       null,
        /* 352 */       null,
        /* 353 */       null,
        /* 354 */       null,
        /* 355 */       null,
        /* 356 */       null,
        /* 357 */       null,
        /* 358 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.ColumnInfo",
        /* 359 */       "com.pivotal.gemfirexd.internal.impl.sql.depend.DepClassInfo",
        /* 360 */       "com.pivotal.gemfirexd.internal.impl.store.access.btree.index.B2IStaticCompiledInfo",
        /* 361 */       null, // SQLData marker
        /* 362 */       null,
        /* 363 */       null,
        /* 364 */       null,
        /* 365 */       null,
        /* 366 */       null,
        /* 367 */       null,
        /* 368 */       null,
        /* 369 */       null,
        /* 370 */       null,
        /* 371 */       "com.pivotal.gemfirexd.internal.impl.sql.catalog.CoreDDFinderClassInfo",             // InstanceGetter
        /* 372 */       null,
        /* 373 */       null,
        /* 374 */       null,
        /* 375 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.UpdatableVTIConstantAction",
        /* 376 */       null,
        /* 377 */       null,
        /* 378 */       null,
        /* 379 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealInsertVTIResultSetStatistics",
        /* 380 */       "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealDeleteVTIResultSetStatistics",
        /* 381 */       null, // Unused,
        /* 382 */       null, // Unused
        /* 383 */   "com.pivotal.gemfirexd.internal.impl.sql.GenericColumnDescriptor",
        /* 384 */   null, // Unused,
        /* 385 */   null,
        /* 386 */   null,
        /* 387 */       "com.pivotal.gemfirexd.internal.catalog.types.IndexDescriptorImpl",
        /* 388 */       "com.pivotal.gemfirexd.internal.impl.store.access.btree.index.B2I_v10_2",
        /* 389 */   null,
        /* 390 */   null,
        /* 391 */   null,
        /* 392 */   null,
        /* 393 */       "com.pivotal.gemfirexd.internal.impl.sql.catalog.CoreDDFinderClassInfo",             // InstanceGetter
        /* 394 */   null,
        /* 395 */   null,
        /* 396 */       null, // Unused
        /* 397 */   "com.pivotal.gemfirexd.internal.catalog.types.StatisticsImpl",
        /* 398 */       null,
        /* 399 */       null,
        /* 400 */   null,
        /* 401 */       "com.pivotal.gemfirexd.internal.impl.sql.catalog.DD_Version",
        /* 402 */       "com.pivotal.gemfirexd.internal.impl.sql.catalog.DD_Version",
        /* 403 + 0 */           null,
        /* 1 */         null,
        /* 2 */         null,
        /* 3 */         null,
        /* 4 */         null,
        /* 5 */         null,
        /* 6 */         null,
        /* 7 */         null,
        /* 8 */         null,
        /* 9 */         null,
        /* 10 */        null,
        /* 11 */        null,
        /* 12 */        null,
        /* 13 */        null,
        /* 14 */        null,
        /* 15 */        null,
        /* 16 */        null,
        /* 17 */        null,
        /* 18 */        null,
        /* 19 */        null,
        /* 20 */        null,
        /* 21 */        null,
        /* 22 */        null,
        /* 23 */        null,
        /* 24 */        null,
        /* 25 */        null,
        /* 26 */        null,
        /* 27 */        null,
        /* 28 */        null,
        /* 29 */        null,
        /* 30 */        null,
        /* 31 */        null,
        /* 32 */        null,
        /* 33 */        null,
        /* 403 + 34 */  null,
    /* 438 */   null,
        /* 439 */   "com.pivotal.gemfirexd.internal.impl.sql.execute.rts.RealDeleteCascadeResultSetStatistics",    

    /// --- BLOB is copying LONGVARBIT in implementation
        /* 440 */   null,
        /* 441 */   "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter, BLOB_COMPILATION_TYPE_ID
        /* 442 */   "com.pivotal.gemfirexd.internal.catalog.types.TypesImplInstanceGetter", // BLOB_TYPE_ID_IMPL
        /* 443 */   "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter, SQL_BLOB_ID

    /// --- CLOB is copying LONGVARCHAR in implementation
        /* 444 */   null,
        /* 445 */   "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter
        /* 446 */   "com.pivotal.gemfirexd.internal.catalog.types.TypesImplInstanceGetter",
        /* 447 */   "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter,
           

    /// --- NLOB is copying NATIONAL LONGVARCHAR in implementation
        
        /* 448 */   null,
        /* 449 */   null,
        /* 450 */   null,

        /* 451 */   "com.pivotal.gemfirexd.internal.catalog.types.RoutineAliasInfo",
		/* 452 */   null,
		/* 453 */   "com.pivotal.gemfirexd.internal.impl.store.raw.log.ChecksumOperation",
		/* 454 */   "com.pivotal.gemfirexd.internal.impl.store.raw.data.CompressSpacePageOperation10_2",
		/* 455 */   "com.pivotal.gemfirexd.internal.catalog.types.SynonymAliasInfo",
        /* 456 */   null,
        /* 457 */   "com.pivotal.gemfirexd.internal.catalog.types.TypesImplInstanceGetter", // XML_TYPE_ID_IMPL
        /* 458 */   "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo", //InstanceGetter, XML_ID
        /* 459 */   "com.pivotal.gemfirexd.internal.impl.store.raw.data.EncryptContainerOperation",
        /* 460 */   "com.pivotal.gemfirexd.internal.impl.store.raw.data.EncryptContainerUndoOperation",
        /* 461 */   "com.pivotal.gemfirexd.internal.impl.sql.catalog.CoreDDFinderClassInfo",
        /* 462 */   "com.pivotal.gemfirexd.internal.impl.sql.catalog.CoreDDFinderClassInfo",
        /* 463 */   "com.pivotal.gemfirexd.internal.impl.sql.catalog.CoreDDFinderClassInfo",
        /* 464 */   "com.pivotal.gemfirexd.internal.iapi.types.SqlXmlUtil",        
		/* 465 */   "com.pivotal.gemfirexd.internal.impl.store.raw.data.CompressSpacePageOperation",
        /* 466 */   "com.pivotal.gemfirexd.internal.impl.store.access.btree.index.B2I_10_3",
        /* 467 */   "com.pivotal.gemfirexd.internal.impl.store.access.heap.Heap",
        /* 468 */   "com.pivotal.gemfirexd.internal.iapi.types.DTSClassInfo",
        /* 469 */   "com.pivotal.gemfirexd.internal.catalog.types.RowMultiSetImpl",
        /* 470 */   "com.pivotal.gemfirexd.internal.impl.store.access.btree.index.B2I",
        /* 471 */   "com.pivotal.gemfirexd.internal.impl.sql.catalog.CoreDDFinderClassInfo",
        /* 472 */       "com.pivotal.gemfirexd.internal.impl.sql.catalog.CoreDDFinderClassInfo",
        /* 473 */       "com.pivotal.gemfirexd.internal.impl.sql.catalog.CoreDDFinderClassInfo",
        /* 474 */       "com.pivotal.gemfirexd.internal.catalog.types.UDTAliasInfo",
};
}
