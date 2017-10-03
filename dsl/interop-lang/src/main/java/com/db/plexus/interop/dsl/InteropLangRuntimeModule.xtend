/**
 * Copyright 2017 Plexus Interop Deutsche Bank AG
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * generated by Xtext 2.12.0
 */
package com.db.plexus.interop.dsl

import com.google.inject.Binder
import org.eclipse.xtext.formatting2.FormatterPreferenceValuesProvider
import org.eclipse.xtext.formatting2.FormatterPreferences
import org.eclipse.xtext.preferences.IPreferenceValuesProvider
import org.eclipse.xtext.scoping.IGlobalScopeProvider
import org.eclipse.xtext.scoping.IScopeProvider
import com.google.inject.name.Names
import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider
import com.db.plexus.interop.dsl.protobuf.scoping.ProtoLangLocalScopeProvider
import com.db.plexus.interop.dsl.protobuf.scoping.ProtoLangGlobalScopeProvider
import org.eclipse.xtext.naming.IQualifiedNameProvider
import com.db.plexus.interop.dsl.protobuf.ProtoLangConfig
import com.db.plexus.interop.dsl.protobuf.scoping.ProtoLangQualifiedNameProvider

/**
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 */
class InteropLangRuntimeModule extends AbstractInteropLangRuntimeModule {
	
	private ProtoLangConfig config

	new() {
		this.config = new ProtoLangConfig();
	}

	new(ProtoLangConfig config) {
		this.config = config
	}

	// workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=495851
	def void configureIPreferenceValuesProvider(Binder binder) {
		binder.bind(IPreferenceValuesProvider).annotatedWith(FormatterPreferences).to(FormatterPreferenceValuesProvider)
	}

	override def Class<? extends IGlobalScopeProvider> bindIGlobalScopeProvider() {
		return typeof(ProtoLangGlobalScopeProvider);
	}

	override configureIScopeProviderDelegate(Binder binder) {
		binder.bind(IScopeProvider).annotatedWith(Names.named(AbstractDeclarativeScopeProvider.NAMED_DELEGATE)).to(
			ProtoLangLocalScopeProvider);
	}

	override Class<? extends IQualifiedNameProvider> bindIQualifiedNameProvider() {
		return typeof(ProtoLangQualifiedNameProvider);
	}

	def configureProtoLangConfig(Binder binder) {
		binder.bind(typeof(ProtoLangConfig)).toInstance(config);
	}
}
