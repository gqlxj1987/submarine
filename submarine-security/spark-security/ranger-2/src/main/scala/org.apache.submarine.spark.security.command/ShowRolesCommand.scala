/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.submarine.spark.security.command

import scala.collection.JavaConverters._
import scala.util.control.NonFatal

import org.apache.hadoop.security.UserGroupInformation
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.catalyst.expressions.{Attribute, AttributeReference}
import org.apache.spark.sql.execution.command.RunnableCommand
import org.apache.spark.sql.types.StringType

import org.apache.submarine.spark.security.{RangerSparkAuditHandler, RangerSparkPlugin, SparkAccessControlException}

case class ShowRolesCommand () extends RunnableCommand {

  override def output: Seq[Attribute] =
    Seq(AttributeReference("Role Name", StringType, nullable = false)())

  override def run(sparkSession: SparkSession): Seq[Row] = {
    try {
      val auditHandler = RangerSparkAuditHandler()
      val currentUser = UserGroupInformation.getCurrentUser.getShortUserName
      val roles = RangerSparkPlugin.getAllRoles(currentUser, auditHandler)
      roles.asScala.map(Row(_))
    } catch {
      case NonFatal(e) => throw new SparkAccessControlException(e.getMessage, e)
    } finally {
      // TODO: support auditHandler.flushAudit()
    }
  }
}
